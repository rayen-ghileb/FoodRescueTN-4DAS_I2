package foodrescuetn4das_i2.matching.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "local_association_candidates")
public class LocalAssociationCandidate {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String name;
    @Column(name = "accepted_category", nullable = false)
    private String acceptedCategory;
    @Column(name = "available_capacity_kg", nullable = false, precision = 12, scale = 2)
    private BigDecimal availableCapacityKg;
    @Column(nullable = false)
    private double latitude;
    @Column(nullable = false)
    private double longitude;
    @Column(nullable = false)
    private boolean verified;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAcceptedCategory() { return acceptedCategory; }
    public void setAcceptedCategory(String acceptedCategory) { this.acceptedCategory = acceptedCategory; }
    public BigDecimal getAvailableCapacityKg() { return availableCapacityKg; }
    public void setAvailableCapacityKg(BigDecimal availableCapacityKg) { this.availableCapacityKg = availableCapacityKg; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
}
