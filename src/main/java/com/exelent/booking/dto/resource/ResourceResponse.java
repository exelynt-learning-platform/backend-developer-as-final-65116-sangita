package com.exelent.booking.dto.resource;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.ResourceType;
import java.math.BigDecimal;
import java.time.Instant;

public record ResourceResponse(
        Long id,
        String name,
        String description,
        ResourceType type,
        String location,
        BigDecimal hourlyRate,
        boolean available,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResourceResponse from(BookableResource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getLocation(),
                resource.getHourlyRate(),
                resource.isAvailable(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
