package foodrescuetn4das_i2.surprise.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_simulations")
public class PaymentSimulation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "amount_tnd", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountTND;

    @Column(nullable = false, length = 50)
    private String status;

    // A simulation can exist before it has been processed.
    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private BasketOrder basketOrder;

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmountTND() {
        return amountTND;
    }

    public void setAmountTND(BigDecimal amountTND) {
        this.amountTND = amountTND;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public BasketOrder getBasketOrder() {
        return basketOrder;
    }

    public void setBasketOrder(BasketOrder basketOrder) {
        this.basketOrder = basketOrder;
    }
}
