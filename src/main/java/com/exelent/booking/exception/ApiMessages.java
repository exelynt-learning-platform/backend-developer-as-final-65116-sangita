package com.exelent.booking.exception;

public final class ApiMessages {

    public static final String RESOURCE_UNAVAILABLE = "This resource is not available";
    public static final String SLOT_ALREADY_BOOKED = "This time slot is already booked";
    public static final String USER_PENDING_ONLY = "Users can only create PENDING reservations";
    public static final String ALREADY_CANCELLED = "Already cancelled";
    public static final String OWN_RESERVATIONS_ONLY = "You can only view your own reservations";
    public static final String RESERVATION_NOT_FOUND = "Reservation not found";
    public static final String CANCELLED_CANNOT_UPDATE = "Cancelled reservations cannot be updated";

    private ApiMessages() {
    }
}
