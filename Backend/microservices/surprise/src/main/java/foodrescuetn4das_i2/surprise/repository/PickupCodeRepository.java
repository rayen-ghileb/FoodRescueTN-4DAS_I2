package foodrescuetn4das_i2.surprise.repository;

import foodrescuetn4das_i2.surprise.entity.PickupCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PickupCodeRepository extends JpaRepository<PickupCode, UUID> {
}
