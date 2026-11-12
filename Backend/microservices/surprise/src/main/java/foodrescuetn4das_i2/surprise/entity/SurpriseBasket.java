package foodrescuetn4das_i2.surprise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "surprise_baskets")
public class SurpriseBasket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // External Donation Offer UUID; no cross-service association.
    @Column(name = "offer_id", nullable = false)
    private UUID offerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 50)
    private String status;

    @OneToMany(mappedBy = "surpriseBasket", fetch = FetchType.LAZY)
    private List<BasketOrder> orders = new ArrayList<>();

    @OneToMany(mappedBy = "surpriseBasket", fetch = FetchType.LAZY)
    private List<BasketDescription> descriptions = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public UUID getOfferId() {
        return offerId;
    }

    public void setOfferId(UUID offerId) {
        this.offerId = offerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BasketOrder> getOrders() {
        return orders;
    }

    public List<BasketDescription> getDescriptions() {
        return descriptions;
    }
}
