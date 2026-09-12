package com.exelent.booking.security;

import com.exelent.booking.domain.User;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiMessages.NOT_AUTHENTICATED);
        }
        return user;
    }
}
