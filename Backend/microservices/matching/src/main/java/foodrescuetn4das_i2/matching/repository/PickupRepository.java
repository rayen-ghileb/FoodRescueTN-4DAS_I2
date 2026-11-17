package foodrescuetn4das_i2.matching.repository;

import foodrescuetn4das_i2.matching.entity.Pickup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PickupRepository extends JpaRepository<Pickup, UUID> {
    boolean existsByDonationMatch_Id(UUID matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pickup p where p.id = :id")
    Optional<Pickup> lockById(@Param("id") UUID id);

    @Query("select distinct p from Pickup p join p.assignments a where a.driverId = :driverId and a.status = 'ASSIGNED'")
    List<Pickup> findActiveByDriver(@Param("driverId") UUID driverId);
}
