package com.exelent.booking.exception;

public class JwtConfigurationException extends IllegalStateException {

    public JwtConfigurationException(String message) {
        super("JWT_SECRET misconfigured: " + message);
    }
}
