package com.exelent.booking.config;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.ResourceType;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.ResourceRepository;
import com.exelent.booking.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final Environment environment;

    @Override
    @Transactional
    public void run(String... args) {
        if (environment.matchesProfiles("prod")) {
            throw new IllegalStateException("Seed data cannot run with the prod profile");
        }
        if (userRepository.count() > 0) {
            return;
        }
        if (seedProperties.adminPassword() == null || seedProperties.adminPassword().isBlank()
                || seedProperties.userPassword() == null || seedProperties.userPassword().isBlank()) {
            throw new IllegalStateException(
                    "app.seed.enabled=true requires app.seed.admin-password and app.seed.user-password"
            );
        }

        List<User> users = seedUsers();
        List<BookableResource> resources = seedResources();
        seedReservations(users.get(1), users.get(2), resources.get(0), resources.get(1));
        log.info("Seed data inserted (admin, user, user2). Disable with app.seed.enabled=false");
    }

    private List<User> seedUsers() {
        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode(seedProperties.adminPassword()))
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        User user = userRepository.save(User.builder()
                .username("user")
                .email("user@gmail.com")
                .password(passwordEncoder.encode(seedProperties.userPassword()))
                .role(Role.USER)
                .enabled(true)
                .build());

        User user2 = userRepository.save(User.builder()
                .username("user2")
                .email("user2@gmail.com")
                .password(passwordEncoder.encode(seedProperties.userPassword()))
                .role(Role.USER)
                .enabled(true)
                .build());

        return List.of(admin, user, user2);
    }

    private List<BookableResource> seedResources() {
        BookableResource room = resourceRepository.save(BookableResource.builder()
                .name("Meeting Room A")
                .description("Room with projector, around 10 people")
                .type(ResourceType.ROOM)
                .location("2nd floor")
                .hourlyRate(new BigDecimal("50.00"))
                .available(true)
                .build());

        BookableResource van = resourceRepository.save(BookableResource.builder()
                .name("Office Van")
                .description("7 seater")
                .type(ResourceType.VEHICLE)
                .location("Parking")
                .hourlyRate(new BigDecimal("35.50"))
                .available(true)
                .build());

        resourceRepository.save(BookableResource.builder()
                .name("Camera Kit")
                .description("DSLR + lens + tripod")
                .type(ResourceType.EQUIPMENT)
                .location("Store room")
                .hourlyRate(new BigDecimal("15.00"))
                .available(true)
                .build());

        return List.of(room, van);
    }

    private void seedReservations(User user, User user2, BookableResource room, BookableResource van) {
        // fixed dates so tests that book near "now" do not collide
        LocalDateTime first = LocalDateTime.of(2026, 12, 1, 9, 0);
        LocalDateTime second = LocalDateTime.of(2026, 12, 2, 9, 0);
        LocalDateTime third = LocalDateTime.of(2026, 12, 3, 9, 0);

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(room)
                .startTime(first)
                .endTime(first.plusHours(2))
                .status(ReservationStatus.PENDING)
                .price(new BigDecimal("100.00"))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(van)
                .startTime(second)
                .endTime(second.plusHours(3))
                .status(ReservationStatus.CONFIRMED)
                .price(new BigDecimal("106.50"))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user2)
                .resource(room)
                .startTime(third)
                .endTime(third.plusHours(1))
                .status(ReservationStatus.CANCELLED)
                .price(new BigDecimal("50.00"))
                .build());
    }
}
