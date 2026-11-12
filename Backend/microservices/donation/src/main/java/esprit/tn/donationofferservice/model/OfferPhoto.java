package esprit.tn.donationofferservice.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class OfferPhoto {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String fileUrl;
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}