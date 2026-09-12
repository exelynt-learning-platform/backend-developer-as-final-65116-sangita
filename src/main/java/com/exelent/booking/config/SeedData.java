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
        if (seedProperties.adminPassword() == null || seedProperties.adminPassword().isBlank()
                || seedProperties.userPassword() == null || seedProperties.userPassword().isBlank()) {
            throw new IllegalStateException(
                    "app.seed.enabled=true requires app.seed.admin-password and app.seed.user-password"
            );
        }

        List<User> users = seedUsers();
        List<BookableResource> resources = seedResources();
        seedReservations(users.get(1), users.get(2), resources.get(0), resources.get(1));
        log.info("Seed data ready (admin, user, user2). Disable with app.seed.enabled=false");
    }

    private List<User> seedUsers() {
        User admin = findOrCreateUser("admin", "admin@gmail.com", Role.ADMIN, seedProperties.adminPassword());
        User user = findOrCreateUser("user", "user@gmail.com", Role.USER, seedProperties.userPassword());
        User user2 = findOrCreateUser("user2", "user2@gmail.com", Role.USER, seedProperties.userPassword());
        return List.of(admin, user, user2);
    }

    private User findOrCreateUser(String username, String email, Role role, String rawPassword) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(username)
                        .email(email)
                        .password(passwordEncoder.encode(rawPassword))
                        .role(role)
                        .enabled(true)
                        .build()));
    }

    private List<BookableResource> seedResources() {
        BookableResource room = findOrCreateResource(
                "Meeting Room A", "Room with projector, around 10 people",
                ResourceType.ROOM, "2nd floor", new BigDecimal("50.00"));
        BookableResource van = findOrCreateResource(
                "Office Van", "7 seater",
                ResourceType.VEHICLE, "Parking", new BigDecimal("35.50"));
        findOrCreateResource(
                "Camera Kit", "DSLR + lens + tripod",
                ResourceType.EQUIPMENT, "Store room", new BigDecimal("15.00"));
        return List.of(room, van);
    }

    private BookableResource findOrCreateResource(
            String name,
            String description,
            ResourceType type,
            String location,
            BigDecimal hourlyRate
    ) {
        return resourceRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> resourceRepository.save(BookableResource.builder()
                        .name(name)
                        .description(description)
                        .type(type)
                        .location(location)
                        .hourlyRate(hourlyRate)
                        .available(true)
                        .build()));
    }

    private void seedReservations(User user, User user2, BookableResource room, BookableResource van) {
        if (reservationRepository.existsByUser_Id(user.getId())
                || reservationRepository.existsByUser_Id(user2.getId())) {
            return;
        }
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
