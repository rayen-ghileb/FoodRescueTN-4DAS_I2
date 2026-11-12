package esprit.tn.donationofferservice.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StockReservation {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String sourceType;
    private UUID sourceId;
    private BigDecimal quantity;
    private LocalDateTime expiresAt;
    private ReservationStatus status;
}