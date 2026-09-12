package com.exelent.booking.dto.resource;

import com.exelent.booking.domain.ResourceType;

public record ResourceFilterRequest(
        ResourceType type,
        Boolean available,
        String name
) {
}
