package esprit.tn.donationofferservice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "donation_offers")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DonationOffer {
    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();
    private UUID donorPartnerId;
    private LocalDateTime pickupWindowStart;
    private LocalDateTime pickupWindowEnd;
    private OfferStatus status;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private List<FoodItem> foodItems = new ArrayList<>();   // 1..*
    private PickupLocation pickupLocation;                  // 1
    private List<OfferPhoto> photos = new ArrayList<>();    // 0..*
}