package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.LocalAssociationCandidate;
import foodrescuetn4das_i2.matching.repository.LocalAssociationCandidateRepository;
import foodrescuetn4das_i2.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@Profile("local")
@RequestMapping("/api/local")
@Tag(name = "LOCAL demo — simulated Partner Registry", description = "Only enabled with spring.profiles.active=local")
public class LocalMatchingController {
    private final LocalAssociationCandidateRepository candidates;
    private final MatchingService service;
    public LocalMatchingController(LocalAssociationCandidateRepository candidates, MatchingService service) {
        this.candidates = candidates;
        this.service = service;
    }

    public record CandidateRequest(@NotNull @Schema(example="550e8400-e29b-41d4-a716-446655440001") UUID id, @NotBlank String name,
            @NotBlank String acceptedCategory, @NotNull @DecimalMin("0.0") BigDecimal availableCapacityKg,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            boolean verified) {}

    public record AutomaticMatchRequest(@NotNull @Schema(example="550e8400-e29b-41d4-a716-446655440000") UUID offerId, @NotBlank @Schema(example="VEGETABLES") String category,
            @NotNull @DecimalMin("0.01") BigDecimal quantityKg,
            @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") Double latitude,
            @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") Double longitude,
            @NotNull @DecimalMin("1") @DecimalMax("100000") Double maxDistanceMeters) {}

    @PostMapping("/candidates")
    @Operation(summary = "Add an example verified association (local testing only)")
    public ResponseEntity<LocalAssociationCandidate> createCandidate(@Valid @RequestBody CandidateRequest r) {
        if (candidates.existsById(r.id())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        LocalAssociationCandidate c = new LocalAssociationCandidate();
        c.setId(r.id()); c.setName(r.name()); c.setAcceptedCategory(r.acceptedCategory());
        c.setAvailableCapacityKg(r.availableCapacityKg()); c.setLatitude(r.latitude());
        c.setLongitude(r.longitude()); c.setVerified(r.verified());
        return ResponseEntity.status(HttpStatus.CREATED).body(candidates.save(c));
    }

    @GetMapping("/candidates")
    @Operation(summary = "List local example associations")
    public List<LocalAssociationCandidate> listCandidates() { return candidates.findAll(); }

    @PostMapping("/matches/nearest")
    @Operation(summary = "Auto-select nearest suitable simulated association using PostGIS")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Nearest match created"), @ApiResponse(responseCode = "400", description = "Invalid request"), @ApiResponse(responseCode = "404", description = "No eligible association"), @ApiResponse(responseCode = "409", description = "Capacity changed")})
    public ResponseEntity<MatchController.MatchResponse> createNearest(@Valid @RequestBody AutomaticMatchRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchController.MatchResponse.from(
            service.createNearestMatch(r.offerId(), r.category(), r.quantityKg(),
                r.latitude(), r.longitude(), r.maxDistanceMeters())));
    }
}
