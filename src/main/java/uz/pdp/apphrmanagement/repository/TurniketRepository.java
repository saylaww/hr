package uz.pdp.apphrmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uz.pdp.apphrmanagement.entity.Turniket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TurniketRepository extends JpaRepository<Turniket, Integer> {

    Optional<Turniket> findByCreatedByAndStatus(UUID createdBy, boolean status);

    @Query("select tur from Turniket tur " +
            "where tur.createdBy = :employeeId and (tur.enterDateTime >= :start or tur.enterDateTime <= :finish)")
    List<Turniket> findAllByCreatedByAndEnterDateTimeAndExitDateTimeBefore(UUID employeeId, LocalDateTime start, LocalDateTime finish);
}
