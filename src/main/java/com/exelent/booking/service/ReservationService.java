package com.exelent.booking.service;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import com.exelent.booking.dto.PagedResponse;
import com.exelent.booking.dto.reservation.ReservationCreateRequest;
import com.exelent.booking.dto.reservation.ReservationFilterRequest;
import com.exelent.booking.dto.reservation.ReservationResponse;
import com.exelent.booking.dto.reservation.ReservationUpdateRequest;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.ReservationSpecifications;
import com.exelent.booking.security.AuthHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceService resourceService;
    private final AuthHelper authHelper;

    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> search(ReservationFilterRequest filter, Pageable pageable) {
        User loggedIn = authHelper.getLoggedInUser();
        Long userId = loggedIn.getRole() == Role.ADMIN ? null : loggedIn.getId();

        return PagedResponse.from(
                reservationRepository
                        .findAll(ReservationSpecifications.withFilters(
                                userId, filter.status(), filter.minPrice(), filter.maxPrice()), pageable)
                        .map(ReservationResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        return ReservationResponse.from(findOwnedOrAdmin(id));
    }

    @Transactional
    public ReservationResponse create(ReservationCreateRequest request) {
        User owner = authHelper.getLoggedInUser();
        BookableResource resource = resourceService.getResource(request.resourceId());

        if (!resource.isAvailable()) {
            throw new ApiException(HttpStatus.CONFLICT, "This resource is not available");
        }
        checkOverlap(resource.getId(), request.startTime(), request.endTime(), null);

        ReservationStatus status = ReservationStatus.PENDING;
        if (request.status() != null) {
            if (owner.getRole() == Role.USER && request.status() != ReservationStatus.PENDING) {
                throw new ApiException(HttpStatus.FORBIDDEN, "Users can only create PENDING reservations");
            }
            status = request.status();
        }

        BigDecimal price = request.price();
        if (price == null) {
            long mins = Duration.between(request.startTime(), request.endTime()).toMinutes();
            BigDecimal hours = BigDecimal.valueOf(mins).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
            price = resource.getHourlyRate().multiply(hours).setScale(2, RoundingMode.HALF_UP);
        } else {
            price = price.setScale(2, RoundingMode.HALF_UP);
        }

        Reservation saved = reservationRepository.save(Reservation.builder()
                .user(owner)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(status)
                .price(price)
                .build());
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        BookableResource resource = resourceService.getResource(request.resourceId());
        checkOverlap(resource.getId(), request.startTime(), request.endTime(), reservation.getId());

        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        reservation.setStatus(request.status());
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation reservation = findOwnedOrAdmin(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, "Already cancelled");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(getById(id));
    }

    private Reservation findOwnedOrAdmin(Long id) {
        Reservation reservation = getById(id);
        User loggedIn = authHelper.getLoggedInUser();
        if (loggedIn.getRole() != Role.ADMIN && !reservation.getUser().getId().equals(loggedIn.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only view your own reservations");
        }
        return reservation;
    }

    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found"));
    }

    private void checkOverlap(Long resourceId, LocalDateTime start, LocalDateTime end, Long excludeId) {
        boolean overlap = reservationRepository.existsOverlappingReservation(
                resourceId,
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED),
                start,
                end,
                excludeId
        );
        if (overlap) {
            throw new ApiException(HttpStatus.CONFLICT, "This time slot is already booked");
        }
    }
}
