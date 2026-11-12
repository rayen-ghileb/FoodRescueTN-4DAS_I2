package esprit.tn.donationofferservice.model;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PickupLocation {
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private String address;
    private Double latitude;
    private Double longitude;
}