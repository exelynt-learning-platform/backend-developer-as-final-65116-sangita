package com.exelent.booking.security;

import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthHelper {

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No logged in user found");
        }
        return user;
    }

    public boolean isAdmin() {
        return getLoggedInUser().getRole() == Role.ADMIN;
    }
}
