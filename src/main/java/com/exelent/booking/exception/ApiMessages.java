package com.exelent.booking.exception;

public final class ApiMessages {

    public static final String RESOURCE_UNAVAILABLE = "This resource is not available";
    public static final String SLOT_ALREADY_BOOKED = "This time slot is already booked";
    public static final String USER_PENDING_ONLY = "Users can only create PENDING reservations";
    public static final String ALREADY_CANCELLED = "Already cancelled";
    public static final String OWN_RESERVATIONS_ONLY = "You can only view your own reservations";
    public static final String RESERVATION_NOT_FOUND = "Reservation not found";
    public static final String CANCELLED_CANNOT_UPDATE = "Cancelled reservations cannot be updated";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String RESOURCE_NOT_FOUND = "Resource not found";
    public static final String RESOURCE_HAS_RESERVATIONS = "Can't delete this resource, it already has reservations";
    public static final String VALIDATION_FAILED = "Validation failed";
    public static final String INVALID_REQUEST_BODY = "Invalid request body";
    public static final String UNSUPPORTED_MEDIA_TYPE = "Unsupported media type";
    public static final String NOT_FOUND = "Not found";
    public static final String ACCESS_DENIED = "Access denied";
    public static final String INTERNAL_ERROR = "Something went wrong";
    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String MIN_PRICE_NEGATIVE = "minPrice cannot be negative";
    public static final String MAX_PRICE_NEGATIVE = "maxPrice cannot be negative";
    public static final String MIN_PRICE_GREATER_THAN_MAX = "minPrice cannot be greater than maxPrice";
    public static final String END_AFTER_START = "endTime must be after startTime";
    public static final String JWT_EXPIRATION_POSITIVE = "jwt.expiration-ms must be greater than 0";
    public static final String PLEASE_LOGIN = "Please login";

    private ApiMessages() {
    }

    public static String cannotSortBy(String property) {
        return "Cannot sort by " + property;
    }

    public static String invalidValue(String name) {
        return "Invalid value for '" + name + "'";
    }
}
