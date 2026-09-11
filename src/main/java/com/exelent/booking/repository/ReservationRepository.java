package com.exelent.booking.repository;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    @Override
    @EntityGraph(attributePaths = {"user", "resource"})
    Optional<Reservation> findById(Long id);

    @Query("""
            SELECT COUNT(r) > 0 FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status IN :statuses
              AND r.id <> :excludeId
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeId") Long excludeId
    );
}
