package foodrescuetn4das_i2.surprise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "basket_descriptions")
public class BasketDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "allergen_info", length = 1000)
    private String allergenInfo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private SurpriseBasket surpriseBasket;

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAllergenInfo() {
        return allergenInfo;
    }

    public void setAllergenInfo(String allergenInfo) {
        this.allergenInfo = allergenInfo;
    }

    public SurpriseBasket getSurpriseBasket() {
        return surpriseBasket;
    }

    public void setSurpriseBasket(SurpriseBasket surpriseBasket) {
        this.surpriseBasket = surpriseBasket;
    }
}
