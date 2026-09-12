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
        if (secret.toLowerCase().contains("replace-with") || secret.toLowerCase().contains("changeme")) {
            throw new JwtConfigurationException("JWT_SECRET looks like a placeholder. Set a real secret.");
        }

        byte[] keyBytes;
        boolean decodedAsBase64 = false;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
            decodedAsBase64 = true;
        } catch (RuntimeException ex) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        if (keyBytes.length < 32) {
            if (decodedAsBase64) {
                throw new JwtConfigurationException(
                        "JWT_SECRET Base64-decoded to " + keyBytes.length
                                + " bytes; HS256 needs at least 32. Use a longer key or a raw string of 32+ characters."
                );
            }
            throw new JwtConfigurationException("JWT_SECRET must be at least 32 bytes for HS256");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
