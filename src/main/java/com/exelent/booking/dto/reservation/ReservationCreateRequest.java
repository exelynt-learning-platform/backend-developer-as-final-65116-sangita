package com.exelent.booking.dto.reservation;

import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.validation.ReservationRequest;
import com.exelent.booking.validation.ValidTimeRange;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ValidTimeRange
public record ReservationCreateRequest(
        @NotNull(message = "resourceId is required")
        Long resourceId,

        @NotNull(message = "startTime is required")
        @Future(message = "startTime must be in the future")
        LocalDateTime startTime,

        @NotNull(message = "endTime is required")
        @Future(message = "endTime must be in the future")
        LocalDateTime endTime,

        @DecimalMin(value = "0.00", message = "price must be greater than or equal to 0.00")
        @Digits(integer = 10, fraction = 2, message = "price must have at most 10 integer digits and 2 decimal places")
        BigDecimal price,

        ReservationStatus status
) implements ReservationRequest {
}
