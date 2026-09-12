package com.exelent.booking.config;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.ResourceType;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.ResourceRepository;
import com.exelent.booking.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User admin = userRepository.save(User.builder()
                .username("admin")
                .email("admin@gmail.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build());

        User user = userRepository.save(User.builder()
                .username("user")
                .email("user@gmail.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .enabled(true)
                .build());

        User user2 = userRepository.save(User.builder()
                .username("user2")
                .email("user2@gmail.com")
                .password(passwordEncoder.encode("User@123"))
                .role(Role.USER)
                .enabled(true)
                .build());

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

        // sample bookings so list/filter has some data
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0).withSecond(0).withNano(0);

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(room)
                .startTime(start)
                .endTime(start.plusHours(2))
                .status(ReservationStatus.PENDING)
                .price(new BigDecimal("100.00"))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user)
                .resource(van)
                .startTime(start.plusDays(1))
                .endTime(start.plusDays(1).plusHours(3))
                .status(ReservationStatus.CONFIRMED)
                .price(new BigDecimal("106.50"))
                .build());

        reservationRepository.save(Reservation.builder()
                .user(user2)
                .resource(room)
                .startTime(start.plusDays(2))
                .endTime(start.plusDays(2).plusHours(1))
                .status(ReservationStatus.CANCELLED)
                .price(new BigDecimal("50.00"))
                .build());

        log.info("Inserted test users: admin / Admin@123 , user / User@123 , user2 / User@123");
        log.info("admin id={}, user id={}, user2 id={}", admin.getId(), user.getId(), user2.getId());
    }
}
