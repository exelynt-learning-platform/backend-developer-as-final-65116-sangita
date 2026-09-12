package com.exelent.booking.service;

import com.exelent.booking.dto.reservation.ReservationSort;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class ReservationSortValidator {

    private ReservationSortValidator() {
    }

    public static void validate(Sort sort) {
        sort.forEach(order -> {
            if (!ReservationSort.ALLOWED.contains(order.getProperty())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, ApiMessages.cannotSortBy(order.getProperty()));
            }
        });
    }
}
