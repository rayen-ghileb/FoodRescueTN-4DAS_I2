package foodrescuetn4das_i2.matching.repository;
import foodrescuetn4das_i2.matching.entity.PickupStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;
public interface PickupStatusHistoryRepository extends JpaRepository<PickupStatusHistory, UUID> {
    List<PickupStatusHistory> findByPickup_IdOrderByChangedAtAsc(UUID pickupId);
}
