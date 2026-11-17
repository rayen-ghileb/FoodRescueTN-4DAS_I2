package foodrescuetn4das_i2.matching.service;

import foodrescuetn4das_i2.matching.entity.*;
import foodrescuetn4das_i2.matching.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class MatchingService {
    private final DonationMatchRepository matchRepo;
    private final PickupRepository pickupRepo;
    private final DriverAssignmentRepository assignmentRepo;
    private final PickupStatusHistoryRepository historyRepo;
    private final LocalAssociationCandidateRepository candidateRepo;
    private final DomainEventRecorder events;

    public MatchingService(DonationMatchRepository matchRepo, PickupRepository pickupRepo,
                           DriverAssignmentRepository assignmentRepo, PickupStatusHistoryRepository historyRepo,
                           LocalAssociationCandidateRepository candidateRepo, DomainEventRecorder events) {
        this.matchRepo = matchRepo;
        this.pickupRepo = pickupRepo;
        this.assignmentRepo = assignmentRepo;
        this.historyRepo = historyRepo;
        this.candidateRepo = candidateRepo;
        this.events = events;
    }

    public DonationMatch createMatch(UUID offerId, UUID associationId, BigDecimal quantityKg) {
        if (offerId == null || associationId == null || quantityKg == null || quantityKg.signum() <= 0)
            throw badRequest("Offer ID, association ID and positive quantity are required");
        DonationMatch match = new DonationMatch();
        match.setOfferId(offerId);
        match.setAssociationId(associationId);
        match.setQuantityKg(quantityKg);
        match.setStatus("PENDING");
        match.setMatchedAt(LocalDateTime.now());
        return matchRepo.save(match);
    }

    public DonationMatch createNearestMatch(UUID offerId, String category, BigDecimal quantityKg,
                                            double latitude, double longitude, double maxDistanceMeters) {
        if (offerId == null || category == null || category.isBlank() || quantityKg == null || quantityKg.signum() <= 0
                || latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180
                || maxDistanceMeters <= 0 || maxDistanceMeters > 100_000) {
            throw badRequest("Invalid auto-match request");
        }
        LocalAssociationCandidate candidate = candidateRepo.nearestSuitable(category, quantityKg,
                        latitude, longitude, maxDistanceMeters)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No verified association with sufficient capacity within the search radius"));
        LocalAssociationCandidate locked = candidateRepo.lockById(candidate.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT, "Candidate disappeared"));
        if (!locked.isVerified() || !locked.getAcceptedCategory().equalsIgnoreCase(category)
                || locked.getAvailableCapacityKg().compareTo(quantityKg) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Association capacity changed; retry");
        }
        locked.setAvailableCapacityKg(locked.getAvailableCapacityKg().subtract(quantityKg));
        candidateRepo.save(locked);
        DonationMatch match = createMatch(offerId, locked.getId(), quantityKg);
        match.setDistanceM((int)Math.round(haversineMetres(latitude, longitude,
                locked.getLatitude(), locked.getLongitude())));
        return matchRepo.save(match);
    }

    private static double haversineMetres(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.pow(Math.sin(dLon / 2), 2);
        return 6_371_000.0 * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    @Transactional(readOnly = true)
    public DonationMatch getMatch(UUID id) {
        return matchRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    public DonationMatch cancelPendingMatch(UUID matchId) {
        DonationMatch match = getMatch(matchId);
        if (!"PENDING".equals(match.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only a pending match can be cancelled directly");
        match.setStatus("CANCELLED");
        releaseLocalCapacity(match);
        events.record("MatchCancelled", match.getId(),
                "{\"matchId\":\"" + match.getId() + "\"}");
        return matchRepo.save(match);
    }

    private void releaseLocalCapacity(DonationMatch match) {
        if (match.getDistanceM() == null) return;
        candidateRepo.lockById(match.getAssociationId()).ifPresent(c -> {
            c.setAvailableCapacityKg(c.getAvailableCapacityKg().add(match.getQuantityKg()));
            candidateRepo.save(c);
        });
    }

    public Pickup createPickup(UUID matchId, LocalDateTime scheduledAt, String actor) {
        if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now()))
            throw badRequest("Pickup must be scheduled in the future");
        DonationMatch match = getMatch(matchId);
        if (!"PENDING".equals(match.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending matches can be scheduled");
        if (pickupRepo.existsByDonationMatch_Id(matchId))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pickup already exists for this match");
        Pickup pickup = new Pickup();
        pickup.setDonationMatch(match);
        pickup.setScheduledAt(scheduledAt);
        pickup.setStatus("SCHEDULED");
        pickup = pickupRepo.saveAndFlush(pickup);
        recordStatus(pickup, null, "SCHEDULED", actor);
        match.setStatus("CONFIRMED");
        matchRepo.save(match);
        events.record("MatchConfirmed", match.getId(), String.format(Locale.ROOT,
                "{\"matchId\":\"%s\",\"offerId\":\"%s\",\"associationId\":\"%s\"}",
                match.getId(), match.getOfferId(), match.getAssociationId()));
        return pickup;
    }

    @Transactional(readOnly = true)
    public Pickup getPickup(UUID id) {
        return pickupRepo.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pickup not found"));
    }

    @Transactional(readOnly = true)
    public List<Pickup> listPickups(UUID driverId) {
        return driverId == null ? pickupRepo.findAll() : pickupRepo.findActiveByDriver(driverId);
    }

    public DriverAssignment assignDriver(UUID pickupId, UUID driverId, String actor) {
        if (driverId == null) throw badRequest("Driver ID is required");
        Pickup pickup = lockedPickup(pickupId);
        if (!"SCHEDULED".equals(pickup.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Driver assignment requires SCHEDULED pickup");
        if (assignmentRepo.existsByPickup_IdAndStatus(pickupId, "ASSIGNED"))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A driver is already assigned");
        DriverAssignment assignment = new DriverAssignment();
        assignment.setPickup(pickup);
        assignment.setDriverId(driverId);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus("ASSIGNED");
        assignment = assignmentRepo.save(assignment);
        pickup.setStatus("ASSIGNED");
        recordStatus(pickup, "SCHEDULED", "ASSIGNED", actor);
        events.record("DriverAssigned", pickup.getId(), String.format(Locale.ROOT,
                "{\"pickupId\":\"%s\",\"driverId\":\"%s\"}", pickupId, driverId));
        return assignment;
    }

    public DriverAssignment cancelAssignment(UUID assignmentId, String actor) {
        DriverAssignment assignment = assignmentRepo.findById(assignmentId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
        Pickup pickup = lockedPickup(assignment.getPickup().getId());
        if (!"ASSIGNED".equals(assignment.getStatus()) || !"ASSIGNED".equals(pickup.getStatus()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only an active assignment can be cancelled");
        assignment.setStatus("CANCELLED");
        pickup.setStatus("SCHEDULED");
        recordStatus(pickup, "ASSIGNED", "SCHEDULED", actor);
        return assignmentRepo.save(assignment);
    }

    public Pickup updatePickupStatus(UUID pickupId, String status, String actor) {
        if (status == null || status.isBlank()) throw badRequest("Status is required");
        Pickup pickup = lockedPickup(pickupId);
        String old = pickup.getStatus();
        String next = status.trim().toUpperCase(Locale.ROOT);
        if (old.equals(next)) return pickup; // Idempotent repeated request.
        boolean valid = switch (old) {
            case "SCHEDULED" -> next.equals("CANCELLED");
            case "ASSIGNED" -> next.equals("IN_TRANSIT") || next.equals("CANCELLED");
            case "IN_TRANSIT" -> next.equals("DELIVERED");
            default -> false;
        };
        if (!valid) throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Invalid status transition: " + old + " -> " + next);
        pickup.setStatus(next);
        if ("DELIVERED".equals(next)) {
            pickup.setDeliveredAt(LocalDateTime.now());
            pickup.getDonationMatch().setStatus("FULFILLED");
            matchRepo.save(pickup.getDonationMatch());
            assignmentRepo.findByPickup_IdAndStatus(pickupId, "ASSIGNED")
                    .forEach(a -> { a.setStatus("COMPLETED"); assignmentRepo.save(a); });
            events.record("DeliveryCompleted", pickup.getId(), String.format(Locale.ROOT,
                    "{\"pickupId\":\"%s\",\"matchId\":\"%s\",\"quantityKg\":%s}",
                    pickup.getId(), pickup.getDonationMatch().getId(),
                    pickup.getDonationMatch().getQuantityKg().toPlainString()));
        }
        if ("CANCELLED".equals(next)) {
            pickup.getDonationMatch().setStatus("CANCELLED");
            releaseLocalCapacity(pickup.getDonationMatch());
            matchRepo.save(pickup.getDonationMatch());
            events.record("PickupCancelled", pickup.getId(), String.format(Locale.ROOT,
                    "{\"pickupId\":\"%s\",\"matchId\":\"%s\"}",
                    pickup.getId(), pickup.getDonationMatch().getId()));
            assignmentRepo.findByPickup_IdAndStatus(pickupId, "ASSIGNED")
                    .forEach(a -> { a.setStatus("CANCELLED"); assignmentRepo.save(a); });
        }
        recordStatus(pickup, old, next, actor);
        return pickupRepo.save(pickup);
    }

    @Transactional(readOnly = true)
    public List<PickupStatusHistory> statusHistory(UUID pickupId) {
        getPickup(pickupId);
        return historyRepo.findByPickup_IdOrderByChangedAtAsc(pickupId);
    }

    private Pickup lockedPickup(UUID id) {
        return pickupRepo.lockById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Pickup not found"));
    }

    private void recordStatus(Pickup pickup, String oldStatus, String newStatus, String actor) {
        PickupStatusHistory history = new PickupStatusHistory();
        history.setPickup(pickup);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(actor == null || actor.isBlank() ? "local-demo" : actor.substring(0, Math.min(100, actor.length())));
        history.setChangedAt(LocalDateTime.now());
        historyRepo.save(history);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
