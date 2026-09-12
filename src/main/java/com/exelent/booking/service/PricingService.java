package com.exelent.booking.service;

import com.exelent.booking.domain.BookableResource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    public BigDecimal resolvePrice(
            BookableResource resource,
            LocalDateTime startTime,
            LocalDateTime endTime,
            BigDecimal requestedPrice
    ) {
        if (requestedPrice != null) {
            return requestedPrice.setScale(2, RoundingMode.HALF_UP);
        }
        long minutes = Duration.between(startTime, endTime).toMinutes();
        BigDecimal hours = BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
        return resource.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);
    }
}
