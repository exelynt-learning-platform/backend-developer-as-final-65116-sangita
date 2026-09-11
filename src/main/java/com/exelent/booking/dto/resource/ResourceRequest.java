package com.exelent.booking.dto.resource;

import com.exelent.booking.domain.ResourceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ResourceRequest(
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @Size(max = 1000, message = "description must be at most 1000 characters")
        String description,

        @NotNull(message = "type is required")
        ResourceType type,

        @Size(max = 200, message = "location must be at most 200 characters")
        String location,

        @NotNull(message = "hourlyRate is required")
        @DecimalMin(value = "0.00", message = "hourlyRate must be greater than or equal to 0.00")
        @Digits(integer = 10, fraction = 2, message = "hourlyRate must have at most 10 integer digits and 2 decimal places")
        BigDecimal hourlyRate,

        Boolean available
) {
}
