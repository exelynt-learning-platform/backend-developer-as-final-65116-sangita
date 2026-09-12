package com.exelent.booking.repository;

import com.exelent.booking.domain.BookableResource;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourceRepository extends JpaRepository<BookableResource, Long>, JpaSpecificationExecutor<BookableResource> {

    Optional<BookableResource> findByNameIgnoreCase(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM BookableResource r WHERE r.id = :id")
    Optional<BookableResource> findByIdForUpdate(@Param("id") Long id);
}
