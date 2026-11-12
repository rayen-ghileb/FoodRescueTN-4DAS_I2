package foodrescuetn4das_i2.matching.repository;

import foodrescuetn4das_i2.matching.entity.DonationMatch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DonationMatchRepository extends JpaRepository<DonationMatch, UUID> {
}
