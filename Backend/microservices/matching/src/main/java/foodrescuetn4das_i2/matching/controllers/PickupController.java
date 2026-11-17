package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.*;
import foodrescuetn4das_i2.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pickups")
@Tag(name = "Pickups", description = "Schedule pickup, assign driver, track delivery and view audit history")
public class PickupController {
    private final MatchingService service;
    public PickupController(MatchingService service) { this.service = service; }

    public record CreatePickupRequest(@NotNull UUID matchId, @NotNull LocalDateTime scheduledAt) {}
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
    public record StatusHistoryResponse(UUID id, String oldStatus, String newStatus,
                                        String changedBy, LocalDateTime changedAt) {
        public static StatusHistoryResponse from(PickupStatusHistory h) {
            return new StatusHistoryResponse(h.getId(), h.getOldStatus(), h.getNewStatus(),
                    h.getChangedBy(), h.getChangedAt());
        }
    }

    @PostMapping
    @Operation(summary = "Schedule a future pickup (one per match)")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Pickup scheduled"), @ApiResponse(responseCode = "400", description = "Invalid date"), @ApiResponse(responseCode = "404", description = "Match not found"), @ApiResponse(responseCode = "409", description = "Duplicate pickup or invalid match status")})
    public ResponseEntity<PickupResponse> create(@Valid @RequestBody CreatePickupRequest r,
            @RequestHeader(value = "X-Actor-Id", required = false) String actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(PickupResponse.from(
                service.createPickup(r.matchId(), r.scheduledAt(), actor)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retrieve one pickup")
    public PickupResponse get(@PathVariable UUID id) { return PickupResponse.from(service.getPickup(id)); }

    @GetMapping
    @Operation(summary = "List all pickups or active assignments for a driver")
    public List<PickupResponse> list(@RequestParam(required = false) UUID driverId) {
        return service.listPickups(driverId).stream().map(PickupResponse::from).toList();
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign a volunteer driver to a scheduled pickup")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Assignment created"), @ApiResponse(responseCode = "404", description = "Pickup not found"), @ApiResponse(responseCode = "409", description = "Pickup not assignable")})
    public ResponseEntity<AssignmentResponse> assign(@PathVariable UUID id,
            @Valid @RequestBody AssignDriverRequest r,
            @RequestHeader(value = "X-Actor-Id", required = false) String actor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AssignmentResponse.from(
                service.assignDriver(id, r.driverId(), actor)));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Advance pickup status: ASSIGNED -> IN_TRANSIT -> DELIVERED")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Status updated"), @ApiResponse(responseCode = "404", description = "Pickup not found"), @ApiResponse(responseCode = "409", description = "Invalid transition")})
    public PickupResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest r,
            @RequestHeader(value = "X-Actor-Id", required = false) String actor) {
        return PickupResponse.from(service.updatePickupStatus(id, r.status(), actor));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "View the chronological status history for a pickup")
    public List<StatusHistoryResponse> history(@PathVariable UUID id) {
        return service.statusHistory(id).stream().map(StatusHistoryResponse::from).toList();
    }
}
