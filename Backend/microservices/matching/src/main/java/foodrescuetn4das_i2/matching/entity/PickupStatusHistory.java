package foodrescuetn4das_i2.matching.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pickup_status_history", indexes = @Index(name = "idx_pickup_history", columnList = "pickup_id"))
public class PickupStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pickup_id", nullable = false)
    private Pickup pickup;

    @Column(length = 32)
    private String oldStatus;
    @Column(nullable = false, length = 32)
    private String newStatus;
    @Column(nullable = false, length = 100)
    private String changedBy;
    @Column(nullable = false)
    private LocalDateTime changedAt;

    public UUID getId() { return id; }
    public Pickup getPickup() { return pickup; }
    public void setPickup(Pickup pickup) { this.pickup = pickup; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public LocalDateTime getChangedAt() { return changedAt; }
    public void setChangedAt(LocalDateTime changedAt) { this.changedAt = changedAt; }
}
