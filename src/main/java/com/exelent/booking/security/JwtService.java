package com.exelent.booking.security;

import com.exelent.booking.config.JwtProperties;
import com.exelent.booking.domain.User;
import com.exelent.booking.exception.JwtConfigurationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    static final String BASE64_PREFIX = "base64:";
    static final String RAW_PREFIX = "raw:";

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = buildKey(jwtProperties.secret());
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.expirationMs());
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(Claims claims) {
        return claims.getSubject();
    }

    public String extractUsername(String token) {
        return extractUsername(parseToken(token));
    }

    public boolean isTokenValid(Claims claims, User user) {
        return extractUsername(claims).equalsIgnoreCase(user.getUsername())
                && !claims.getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token, User user) {
        return isTokenValid(parseToken(token), user);
    }

    private static SecretKey buildKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new JwtConfigurationException("JWT_SECRET is required. Set it before starting the app.");
        }

        String trimmed = secret.trim();
        boolean base64 = false;
        String value = trimmed;
        if (startsWithIgnoreCase(trimmed, BASE64_PREFIX)) {
            base64 = true;
            value = trimmed.substring(BASE64_PREFIX.length());
        } else if (startsWithIgnoreCase(trimmed, RAW_PREFIX)) {
            value = trimmed.substring(RAW_PREFIX.length());
        }

        if (value.isBlank()) {
            throw new JwtConfigurationException("JWT_SECRET is empty after the encoding prefix.");
        }
        if (value.toLowerCase().contains("replace-with") || value.toLowerCase().contains("changeme")) {
            throw new JwtConfigurationException("JWT_SECRET looks like a placeholder. Set a real secret.");
        }

        byte[] keyBytes;
        if (base64) {
            try {
                keyBytes = Decoders.BASE64.decode(value);
            } catch (RuntimeException ex) {
                throw new JwtConfigurationException("JWT_SECRET has prefix base64: but is not valid Base64.");
            }
        } else {
            keyBytes = value.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            if (base64) {
                throw new JwtConfigurationException(
                        "JWT_SECRET Base64-decoded to " + keyBytes.length
                                + " bytes; HS256 needs at least 32."
                );
            }
            throw new JwtConfigurationException("JWT_SECRET must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private static boolean startsWithIgnoreCase(String value, String prefix) {
        return value.regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
