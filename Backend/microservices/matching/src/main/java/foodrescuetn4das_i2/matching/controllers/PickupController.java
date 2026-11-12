package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.DriverAssignment;
import foodrescuetn4das_i2.matching.entity.Pickup;
import foodrescuetn4das_i2.matching.service.MatchingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/pickups")
public class PickupController {

    private final MatchingService service;

    public PickupController(MatchingService service) {
        this.service = service;
    }

    public record CreatePickupRequest(@NotNull UUID matchId,
                                      @NotNull LocalDateTime scheduledAt) {}
    public record AssignDriverRequest(@NotNull UUID driverId) {}
    public record UpdateStatusRequest(@NotBlank String status) {}

    public record PickupResponse(UUID id, UUID matchId, LocalDateTime scheduledAt,
                                 String status, LocalDateTime deliveredAt) {
        public static PickupResponse from(Pickup p) {
            return new PickupResponse(p.getId(), p.getDonationMatch().getId(),
                    p.getScheduledAt(), p.getStatus(), p.getDeliveredAt());
        }
    }

    public record AssignmentResponse(UUID id, UUID pickupId, UUID driverId,
                                     LocalDateTime assignedAt, String status) {
        public static AssignmentResponse from(DriverAssignment a) {
            return new AssignmentResponse(a.getId(), a.getPickup().getId(),
                    a.getDriverId(), a.getAssignedAt(), a.getStatus());
        }
    }

    @PostMapping
    public ResponseEntity<PickupResponse> create(@Valid @RequestBody CreatePickupRequest request) {
        Pickup pickup = service.createPickup(request.matchId(), request.scheduledAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(PickupResponse.from(pickup));
    }

    @GetMapping("/{id}")
    public PickupResponse get(@PathVariable UUID id) {
        return PickupResponse.from(service.getPickup(id));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<AssignmentResponse> assign(
            @PathVariable UUID id, @Valid @RequestBody AssignDriverRequest request) {
        DriverAssignment assignment = service.assignDriver(id, request.driverId());
        return ResponseEntity.status(HttpStatus.CREATED).body(AssignmentResponse.from(assignment));
    }

    @PatchMapping("/{id}/status")
    public PickupResponse updateStatus(
            @PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return PickupResponse.from(service.updatePickupStatus(id, request.status()));
    }
}
