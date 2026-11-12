package foodrescuetn4das_i2.matching.service;

import foodrescuetn4das_i2.matching.entity.DonationMatch;
import foodrescuetn4das_i2.matching.entity.DriverAssignment;
import foodrescuetn4das_i2.matching.entity.Pickup;
import foodrescuetn4das_i2.matching.repository.DonationMatchRepository;
import foodrescuetn4das_i2.matching.repository.DriverAssignmentRepository;
import foodrescuetn4das_i2.matching.repository.PickupRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class MatchingService {

    private final DonationMatchRepository matchRepo;
    private final PickupRepository pickupRepo;
    private final DriverAssignmentRepository assignmentRepo;

    public MatchingService(DonationMatchRepository matchRepo,
                           PickupRepository pickupRepo,
                           DriverAssignmentRepository assignmentRepo) {
        this.matchRepo = matchRepo;
        this.pickupRepo = pickupRepo;
        this.assignmentRepo = assignmentRepo;
    }

    public DonationMatch createMatch(UUID offerId, UUID associationId, BigDecimal quantityKg) {
        DonationMatch match = new DonationMatch();
        match.setOfferId(offerId);
        match.setAssociationId(associationId);
        match.setQuantityKg(quantityKg);
        match.setStatus("PENDING");
        match.setMatchedAt(LocalDateTime.now());
        return matchRepo.save(match);
    }

    @Transactional(readOnly = true)
    public DonationMatch getMatch(UUID id) {
        return matchRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
    }

    public Pickup createPickup(UUID matchId, LocalDateTime scheduledAt) {
        DonationMatch match = getMatch(matchId);
        if (pickupRepo.existsByDonationMatch_Id(matchId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pickup already exists for this match");
        }

        Pickup pickup = new Pickup();
        pickup.setDonationMatch(match);
        pickup.setScheduledAt(scheduledAt);
        pickup.setStatus("SCHEDULED");
        return pickupRepo.save(pickup);
    }

    @Transactional(readOnly = true)
    public Pickup getPickup(UUID id) {
        return pickupRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pickup not found"));
    }

    public DriverAssignment assignDriver(UUID pickupId, UUID driverId) {
        Pickup pickup = getPickup(pickupId);
        if ("DELIVERED".equals(pickup.getStatus()) || "CANCELLED".equals(pickup.getStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot assign a driver to a closed pickup");
        }
        if (assignmentRepo.existsByPickup_IdAndStatus(pickupId, "ASSIGNED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An active driver is already assigned");
        }

        DriverAssignment assignment = new DriverAssignment();
        assignment.setPickup(pickup);
        assignment.setDriverId(driverId);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setStatus("ASSIGNED");
        return assignmentRepo.save(assignment);
    }

    public Pickup updatePickupStatus(UUID pickupId, String status) {
        Pickup pickup = getPickup(pickupId);
        String newStatus = status.trim().toUpperCase(Locale.ROOT);

        if (!Set.of("SCHEDULED", "PICKED_UP", "DELIVERED", "CANCELLED").contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pickup status");
        }
        if (("DELIVERED".equals(pickup.getStatus()) || "CANCELLED".equals(pickup.getStatus()))
                && !pickup.getStatus().equals(newStatus)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Pickup is already closed");
        }

        pickup.setStatus(newStatus);
        if ("DELIVERED".equals(newStatus) && pickup.getDeliveredAt() == null) {
            pickup.setDeliveredAt(LocalDateTime.now());
        } else if (!"DELIVERED".equals(newStatus)) {
            pickup.setDeliveredAt(null);
        }
        return pickupRepo.save(pickup);
    }
}
