package com.exelent.booking.service;

import com.exelent.booking.config.JwtProperties;
import com.exelent.booking.domain.User;
import com.exelent.booking.dto.auth.LoginRequest;
import com.exelent.booking.dto.auth.LoginResponse;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import com.exelent.booking.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            User user = (User) auth.getPrincipal();
            return new LoginResponse(
                    jwtService.generateToken(user),
                    "Bearer",
                    jwtProperties.expirationMs(),
                    user.getUsername(),
                    user.getRole().name()
            );
        } catch (AuthenticationException e) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ApiMessages.INVALID_CREDENTIALS);
        }
    }
}
