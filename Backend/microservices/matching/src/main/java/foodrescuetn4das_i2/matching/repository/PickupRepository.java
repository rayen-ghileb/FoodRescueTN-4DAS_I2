package foodrescuetn4das_i2.matching.repository;

import foodrescuetn4das_i2.matching.entity.Pickup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PickupRepository extends JpaRepository<Pickup, UUID> {
    boolean existsByDonationMatch_Id(UUID matchId);
}
