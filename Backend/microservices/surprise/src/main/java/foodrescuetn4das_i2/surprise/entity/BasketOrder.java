package foodrescuetn4das_i2.surprise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "basket_orders")
public class BasketOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // External User Service UUID; no cross-service association.
    @Column(name = "buyer_user_id", nullable = false)
    private UUID buyerUserId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false, length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "basket_id", nullable = false)
    private SurpriseBasket surpriseBasket;

    @OneToOne(mappedBy = "basketOrder", fetch = FetchType.LAZY)
    private PickupCode pickupCode;

    @OneToOne(mappedBy = "basketOrder", fetch = FetchType.LAZY)
    private PaymentSimulation paymentSimulation;

    public UUID getId() {
        return id;
    }

    public UUID getBuyerUserId() {
        return buyerUserId;
    }

    public void setBuyerUserId(UUID buyerUserId) {
        this.buyerUserId = buyerUserId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SurpriseBasket getSurpriseBasket() {
        return surpriseBasket;
    }

    public void setSurpriseBasket(SurpriseBasket surpriseBasket) {
        this.surpriseBasket = surpriseBasket;
    }

    public PickupCode getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(PickupCode pickupCode) {
        this.pickupCode = pickupCode;
    }

    public PaymentSimulation getPaymentSimulation() {
        return paymentSimulation;
    }

    public void setPaymentSimulation(PaymentSimulation paymentSimulation) {
        this.paymentSimulation = paymentSimulation;
    }
}
