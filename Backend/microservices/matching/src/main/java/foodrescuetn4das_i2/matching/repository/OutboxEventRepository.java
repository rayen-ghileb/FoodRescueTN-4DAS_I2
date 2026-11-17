package foodrescuetn4das_i2.matching.repository;
import foodrescuetn4das_i2.matching.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {}
