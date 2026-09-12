package com.exelent.booking.security;

import com.exelent.booking.config.JwtProperties;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(new JwtProperties(
                "TestOnlySecretKeyThatIsAtLeastThirtyTwoBytesLong!!",
                3_600_000
        ));
    }

    @Test
    void generatesAndValidatesTokenForUser() {
        User user = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@booking.local")
                .password("encoded")
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("admin");
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void rejectsTokenForDifferentUser() {
        User admin = User.builder().id(1L).username("admin").email("a@b.c").password("x").role(Role.ADMIN).enabled(true).build();
        User user = User.builder().id(2L).username("user").email("u@b.c").password("x").role(Role.USER).enabled(true).build();

        String token = jwtService.generateToken(admin);

        assertThat(jwtService.isTokenValid(token, user)).isFalse();
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("too-short", 1000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET");
    }

    @Test
    void rejectsMissingSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("  ", 1000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET is required");
    }

    @Test
    void rejectsShortBase64Secret() {
        String shortBase64 = "base64:" + java.util.Base64.getEncoder().encodeToString("too-short".getBytes());
        assertThatThrownBy(() -> new JwtService(new JwtProperties(shortBase64, 1000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Base64-decoded");
    }

    @Test
    void acceptsExplicitRawPrefix() {
        JwtService service = new JwtService(new JwtProperties(
                "raw:TestOnlySecretKeyThatIsAtLeastThirtyTwoBytesLong!!",
                3_600_000
        ));
        User user = User.builder().id(1L).username("admin").email("a@b.c").password("x").role(Role.ADMIN).enabled(true).build();
        assertThat(service.extractUsername(service.generateToken(user))).isEqualTo("admin");
    }

    @Test
    void acceptsExplicitBase64Prefix() {
        byte[] key = new byte[32];
        java.util.Arrays.fill(key, (byte) 7);
        JwtService service = new JwtService(new JwtProperties(
                "base64:" + java.util.Base64.getEncoder().encodeToString(key),
                3_600_000
        ));
        User user = User.builder().id(1L).username("admin").email("a@b.c").password("x").role(Role.ADMIN).enabled(true).build();
        assertThat(service.isTokenValid(service.generateToken(user), user)).isTrue();
    }

    @Test
    void doesNotTreatRawAlphabetAsBase64() {
        // without a prefix the secret is used as UTF-8, even if it looks like Base64
        String looksLikeBase64 = java.util.Base64.getEncoder()
                .encodeToString("TestOnlySecretKeyThatIsAtLeast32Bytes!!".getBytes());
        JwtService service = new JwtService(new JwtProperties(looksLikeBase64, 3_600_000));
        User user = User.builder().id(1L).username("admin").email("a@b.c").password("x").role(Role.ADMIN).enabled(true).build();
        assertThat(service.extractUsername(service.generateToken(user))).isEqualTo("admin");
    }

    @Test
    void rejectsNonPositiveExpiration() {
        assertThatThrownBy(() -> new JwtProperties("TestOnlySecretKeyThatIsAtLeastThirtyTwoBytesLong!!", 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("expiration-ms");
    }

    @Test
    void rejectsPlaceholderSecret() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties(
                "replace-with-a-long-random-secret-key-min-32-chars",
                1000
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("placeholder");
    }
}
