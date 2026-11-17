package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.DonationMatch;
import foodrescuetn4das_i2.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "Donation matches", description = "Create a match and retrieve its details")
public class MatchController {
    private final MatchingService service;
    public MatchController(MatchingService service) { this.service = service; }

    public record CreateMatchRequest(@NotNull UUID offerId, @NotNull UUID associationId,
                                     @NotNull @DecimalMin("0.01") BigDecimal quantityKg) {}
    public record MatchResponse(UUID id, UUID offerId, UUID associationId,
            BigDecimal quantityKg, String status, LocalDateTime matchedAt, Integer distanceM) {
        public static MatchResponse from(DonationMatch m) {
            return new MatchResponse(m.getId(), m.getOfferId(), m.getAssociationId(),
                    m.getQuantityKg(), m.getStatus(), m.getMatchedAt(), m.getDistanceM());
        }
    }

    @PostMapping
    @Operation(summary = "Create a manual match (stub IDs until other services are integrated)")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Match created"), @ApiResponse(responseCode = "400", description = "Invalid match request")})
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchResponse.from(
                service.createMatch(r.offerId(), r.associationId(), r.quantityKg())));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a pending match before pickup scheduling")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Match cancelled"), @ApiResponse(responseCode = "404", description = "Not found"), @ApiResponse(responseCode = "409", description = "Match is not pending")})
    public MatchResponse cancel(@PathVariable UUID id) {
        return MatchResponse.from(service.cancelPendingMatch(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a match by ID")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Match returned"), @ApiResponse(responseCode = "404", description = "Not found")})
    public MatchResponse get(@PathVariable UUID id) { return MatchResponse.from(service.getMatch(id)); }
}
