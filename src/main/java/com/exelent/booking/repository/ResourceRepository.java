package com.exelent.booking.repository;

import com.exelent.booking.domain.BookableResource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<BookableResource, Long> {
}
