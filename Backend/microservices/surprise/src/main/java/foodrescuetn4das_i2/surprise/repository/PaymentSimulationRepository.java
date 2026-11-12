package foodrescuetn4das_i2.surprise.repository;

import foodrescuetn4das_i2.surprise.entity.PaymentSimulation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentSimulationRepository extends JpaRepository<PaymentSimulation, UUID> {
}
