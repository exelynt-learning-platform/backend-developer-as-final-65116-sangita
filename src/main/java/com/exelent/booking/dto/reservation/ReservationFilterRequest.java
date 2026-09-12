package com.exelent.booking.dto.reservation;

import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.exception.ApiMessages;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ReservationFilterRequest(
        ReservationStatus status,

        @DecimalMin(value = "0.00", message = ApiMessages.MIN_PRICE_NEGATIVE)
        BigDecimal minPrice,

        @DecimalMin(value = "0.00", message = ApiMessages.MAX_PRICE_NEGATIVE)
        BigDecimal maxPrice
) {
    @AssertTrue(message = ApiMessages.MIN_PRICE_GREATER_THAN_MAX)
    public boolean isPriceRangeValid() {
        return minPrice == null || maxPrice == null || minPrice.compareTo(maxPrice) <= 0;
    }
}
