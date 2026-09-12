package com.exelent.booking.repository;

import com.exelent.booking.domain.BookableResource;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<BookableResource, Long> {

    Optional<BookableResource> findByNameIgnoreCase(String name);
}
