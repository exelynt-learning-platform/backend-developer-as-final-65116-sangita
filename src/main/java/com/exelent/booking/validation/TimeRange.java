package com.exelent.booking.validation;

import java.time.LocalDateTime;

public interface TimeRange {

    LocalDateTime startTime();

    LocalDateTime endTime();
}
