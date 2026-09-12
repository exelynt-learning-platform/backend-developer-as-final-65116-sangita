package com.exelent.booking.dto.reservation;

import java.util.Set;

public final class ReservationSort {

    public static final String DEFAULT = "createdAt";

    public static final Set<String> ALLOWED = Set.of(
            "id", "price", "status", "startTime", "endTime", "createdAt", "updatedAt"
    );

    private ReservationSort() {
    }
}
