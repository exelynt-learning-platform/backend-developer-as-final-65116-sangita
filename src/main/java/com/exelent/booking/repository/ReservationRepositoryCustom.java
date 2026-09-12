package com.exelent.booking.repository;

import com.exelent.booking.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;

public interface ReservationRepositoryCustom {

    @EntityGraph(attributePaths = {"user", "resource"})
    Page<Reservation> search(Specification<Reservation> spec, Pageable pageable);
}
