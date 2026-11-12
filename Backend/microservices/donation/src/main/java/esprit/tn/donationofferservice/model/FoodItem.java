package esprit.tn.donationofferservice.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FoodItem {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String name;
    private String category;
    private BigDecimal quantityAvailable;
    private String unit;
    private LocalDateTime expiresAt;
    @Builder.Default
    private List<StockReservation> reservations = new ArrayList<>();
}
