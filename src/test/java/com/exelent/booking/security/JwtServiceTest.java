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
                .hasMessageContaining("32 bytes");
    }
}
