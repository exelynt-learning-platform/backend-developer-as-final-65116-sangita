package com.exelent.booking.service;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import com.exelent.booking.security.AuthHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservationAccessPolicy {

    private final AuthHelper authHelper;

    public User currentUser() {
        return authHelper.getLoggedInUser();
    }

    public Long listScopeUserId(User loggedIn) {
        return loggedIn.getRole() == Role.ADMIN ? null : loggedIn.getId();
    }

    public void assertCanCreateWithStatus(User owner, ReservationStatus requestedStatus) {
        if (requestedStatus != null && owner.getRole() == Role.USER && requestedStatus != ReservationStatus.PENDING) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiMessages.USER_PENDING_ONLY);
        }
    }

    public void requireOwnerOrAdmin(Reservation reservation) {
        User loggedIn = currentUser();
        if (loggedIn.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(loggedIn.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiMessages.OWN_RESERVATIONS_ONLY);
        }
    }
}
