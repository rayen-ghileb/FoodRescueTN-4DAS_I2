package foodrescuetn4das_i2.surprise.repository;

import foodrescuetn4das_i2.surprise.entity.BasketOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BasketOrderRepository extends JpaRepository<BasketOrder, UUID> {
}
