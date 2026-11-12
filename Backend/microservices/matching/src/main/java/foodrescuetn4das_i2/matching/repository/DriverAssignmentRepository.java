package foodrescuetn4das_i2.matching.repository;

import foodrescuetn4das_i2.matching.entity.DriverAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, UUID> {
    boolean existsByPickup_IdAndStatus(UUID pickupId, String status);
}
