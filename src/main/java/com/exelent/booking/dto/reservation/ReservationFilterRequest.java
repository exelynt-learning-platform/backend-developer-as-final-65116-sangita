package com.exelent.booking.dto.reservation;

import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.exception.ApiException;
import java.math.BigDecimal;
import org.springframework.http.HttpStatus;

public record ReservationFilterRequest(
        ReservationStatus status,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
    public void validate() {
        if (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "minPrice cannot be greater than maxPrice");
        }
    }
}
