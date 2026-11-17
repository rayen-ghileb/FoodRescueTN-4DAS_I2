package foodrescuetn4das_i2.matching.repository;

import foodrescuetn4das_i2.matching.entity.LocalAssociationCandidate;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface LocalAssociationCandidateRepository extends JpaRepository<LocalAssociationCandidate, UUID> {
    @Query(value = """
        SELECT c.* FROM local_association_candidates c
        WHERE c.verified = true
          AND LOWER(c.accepted_category) = LOWER(:category)
          AND c.available_capacity_kg >= :quantityKg
          AND ST_DistanceSphere(
                ST_SetSRID(ST_MakePoint(c.longitude, c.latitude), 4326),
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) <= :maxDistanceMeters
        ORDER BY ST_DistanceSphere(
                ST_SetSRID(ST_MakePoint(c.longitude, c.latitude), 4326),
                ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) ASC
        LIMIT 1
        """, nativeQuery = true)
    Optional<LocalAssociationCandidate> nearestSuitable(
            @Param("category") String category,
            @Param("quantityKg") BigDecimal quantityKg,
            @Param("latitude") double latitude,
            @Param("longitude") double longitude,
            @Param("maxDistanceMeters") double maxDistanceMeters);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from LocalAssociationCandidate c where c.id = :id")
    Optional<LocalAssociationCandidate> lockById(@Param("id") UUID id);
}
