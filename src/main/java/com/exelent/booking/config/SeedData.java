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
import com.exelent.booking.service.PricingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile({"dev", "h2"})
@ConditionalOnProperty(prefix = "app.seed", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SeedData implements CommandLineRunner {

    static final String DEMO_ADMIN_PASSWORD = "Admin@123";
    static final String DEMO_USER_PASSWORD = "User@123";
    static final String ROOM_NAME = "Meeting Room A";
    static final String VAN_NAME = "Office Van";
    static final String CAMERA_NAME = "Camera Kit";
    static final int ROOM_PENDING_HOURS = 2;
    static final int VAN_CONFIRMED_HOURS = 3;
    static final int ROOM_CANCELLED_HOURS = 1;

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;
    private final Environment environment;
    private final PricingService pricingService;

    @Override
    @Transactional
    public void run(String... args) {
        if (environment.matchesProfiles("prod", "production", "staging", "stage", "uat", "qa")) {
            throw new IllegalStateException("Seed data cannot run with a production-adjacent profile");
        }
        if (seedProperties.adminPassword() == null || seedProperties.adminPassword().isBlank()
                || seedProperties.userPassword() == null || seedProperties.userPassword().isBlank()) {
            throw new IllegalStateException(
                    "app.seed.enabled=true requires app.seed.admin-password and app.seed.user-password"
            );
        }
        boolean demoPasswords = DEMO_ADMIN_PASSWORD.equals(seedProperties.adminPassword())
                || DEMO_USER_PASSWORD.equals(seedProperties.userPassword());
        if (demoPasswords && !environment.matchesProfiles("dev", "h2")) {
            throw new IllegalStateException(
                    "Documented demo passwords cannot be used outside the dev/h2 profiles. "
                            + "Set SEED_ADMIN_PASSWORD and SEED_USER_PASSWORD."
            );
        }
        if (demoPasswords) {
            log.warn("Seed is using documented demo passwords. Override with SEED_ADMIN_PASSWORD and SEED_USER_PASSWORD.");
        }

        User regularUser = findOrCreateUser("user", "user@gmail.com", Role.USER, seedProperties.userPassword());
        User user2 = findOrCreateUser("user2", "user2@gmail.com", Role.USER, seedProperties.userPassword());
        findOrCreateUser("admin", "admin@gmail.com", Role.ADMIN, seedProperties.adminPassword());

        Map<String, BookableResource> resources = seedResources();
        seedReservations(regularUser, user2, resources.get(ROOM_NAME), resources.get(VAN_NAME));
        log.info("Seed data ready (admin, user, user2). Disable with app.seed.enabled=false");
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

    private Map<String, BookableResource> seedResources() {
        Map<String, BookableResource> resources = new LinkedHashMap<>();
        resources.put(ROOM_NAME, findOrCreateResource(
                ROOM_NAME, "Room with projector, around 10 people",
                ResourceType.ROOM, "2nd floor", new BigDecimal("50.00")));
        resources.put(VAN_NAME, findOrCreateResource(
                VAN_NAME, "7 seater",
                ResourceType.VEHICLE, "Parking", new BigDecimal("35.50")));
        resources.put(CAMERA_NAME, findOrCreateResource(
                CAMERA_NAME, "DSLR + lens + tripod",
                ResourceType.EQUIPMENT, "Store room", new BigDecimal("15.00")));
        return resources;
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
        LocalDateTime firstStart = LocalDateTime.of(2026, 12, 1, 9, 0);
        LocalDateTime firstEnd = firstStart.plusHours(ROOM_PENDING_HOURS);
        LocalDateTime secondStart = LocalDateTime.of(2026, 12, 2, 9, 0);
        LocalDateTime secondEnd = secondStart.plusHours(VAN_CONFIRMED_HOURS);
        LocalDateTime thirdStart = LocalDateTime.of(2026, 12, 3, 9, 0);
        LocalDateTime thirdEnd = thirdStart.plusHours(ROOM_CANCELLED_HOURS);

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(room)
                .startTime(firstStart)
                .endTime(firstEnd)
                .status(ReservationStatus.PENDING)
                .price(pricingService.resolvePrice(room, firstStart, firstEnd, null))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(van)
                .startTime(secondStart)
                .endTime(secondEnd)
                .status(ReservationStatus.CONFIRMED)
                .price(pricingService.resolvePrice(van, secondStart, secondEnd, null))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user2)
                .resource(room)
                .startTime(thirdStart)
                .endTime(thirdEnd)
                .status(ReservationStatus.CANCELLED)
                .price(pricingService.resolvePrice(room, thirdStart, thirdEnd, null))
                .build());
    }
}
