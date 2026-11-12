package foodrescuetn4das_i2.surprise.repository;

import foodrescuetn4das_i2.surprise.entity.BasketDescription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BasketDescriptionRepository extends JpaRepository<BasketDescription, UUID> {
}
