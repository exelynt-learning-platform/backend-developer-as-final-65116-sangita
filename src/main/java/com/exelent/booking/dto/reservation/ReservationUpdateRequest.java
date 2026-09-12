package com.exelent.booking.dto.reservation;

import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.validation.ReservationRequest;
import com.exelent.booking.validation.ValidTimeRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ValidTimeRange
public record ReservationUpdateRequest(
        @NotNull(message = "resourceId is required")
        Long resourceId,

        @NotNull(message = "startTime is required")
        LocalDateTime startTime,

        @NotNull(message = "endTime is required")
        LocalDateTime endTime,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.00", message = "price must be greater than or equal to 0.00")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 10 integer digits and 2 decimal places")
        BigDecimal price,

        @NotNull(message = "status is required")
        ReservationStatus status
) implements ReservationRequest {
}
