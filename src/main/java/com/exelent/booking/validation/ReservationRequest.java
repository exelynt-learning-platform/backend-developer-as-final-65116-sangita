package com.exelent.booking.validation;

import com.exelent.booking.domain.ReservationStatus;
import java.math.BigDecimal;

public interface ReservationRequest extends TimeRange {

    Long resourceId();

    BigDecimal price();

    ReservationStatus status();
}
