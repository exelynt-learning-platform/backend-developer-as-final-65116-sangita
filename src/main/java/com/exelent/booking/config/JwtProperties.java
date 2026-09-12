package com.exelent.booking.config;

import com.exelent.booking.exception.ApiMessages;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        @Positive(message = ApiMessages.JWT_EXPIRATION_POSITIVE)
        long expirationMs
) {
    public JwtProperties {
        if (expirationMs <= 0) {
            throw new IllegalStateException(ApiMessages.JWT_EXPIRATION_POSITIVE);
        }
    }
}
