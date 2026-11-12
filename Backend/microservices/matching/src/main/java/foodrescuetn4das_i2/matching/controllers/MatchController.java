package foodrescuetn4das_i2.matching.controllers;

import foodrescuetn4das_i2.matching.entity.DonationMatch;
import foodrescuetn4das_i2.matching.service.MatchingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchingService service;

    public MatchController(MatchingService service) {
        this.service = service;
    }

    public record CreateMatchRequest(
            @NotNull UUID offerId,
            @NotNull UUID associationId,
            @NotNull @DecimalMin("0.01") BigDecimal quantityKg
    ) {}

    public record MatchResponse(
            UUID id, UUID offerId, UUID associationId,
            BigDecimal quantityKg, String status, LocalDateTime matchedAt
    ) {
        public static MatchResponse from(DonationMatch m) {
            return new MatchResponse(m.getId(), m.getOfferId(), m.getAssociationId(),
                    m.getQuantityKg(), m.getStatus(), m.getMatchedAt());
        }
    }

    @PostMapping
    public ResponseEntity<MatchResponse> create(@Valid @RequestBody CreateMatchRequest request) {
        DonationMatch match = service.createMatch(request.offerId(), request.associationId(), request.quantityKg());
        return ResponseEntity.status(HttpStatus.CREATED).body(MatchResponse.from(match));
    }

    @GetMapping("/{id}")
    public MatchResponse get(@PathVariable UUID id) {
        return MatchResponse.from(service.getMatch(id));
    }
}
