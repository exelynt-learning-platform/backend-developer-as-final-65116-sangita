package com.exelent.booking.repository;

import com.exelent.booking.domain.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface ReservationRepositoryCustom {

    Page<Reservation> search(Specification<Reservation> spec, Pageable pageable);
}
