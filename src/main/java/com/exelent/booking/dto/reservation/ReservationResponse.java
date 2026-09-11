package com.exelent.booking.dto.reservation;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

public record ReservationResponse(
        Long id,
        Long userId,
        String username,
        Long resourceId,
        String resourceName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        BigDecimal price,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getUsername(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus(),
                reservation.getPrice(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
