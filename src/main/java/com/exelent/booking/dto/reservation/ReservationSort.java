package com.exelent.booking.dto.reservation;

import com.exelent.booking.exception.ApiException;
import java.util.Set;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class ReservationSort {

    public static final String DEFAULT = "createdAt";

    public static final Set<String> ALLOWED = Set.of(
            "id", "price", "status", "startTime", "endTime", "createdAt", "updatedAt"
    );

    private ReservationSort() {
    }

    public static void validate(Sort sort) {
        sort.forEach(order -> {
            if (!ALLOWED.contains(order.getProperty())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot sort by " + order.getProperty());
            }
        });
    }
}
