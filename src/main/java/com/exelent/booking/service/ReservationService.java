package com.exelent.booking.service;

import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.User;
import com.exelent.booking.dto.PagedResponse;
import com.exelent.booking.dto.reservation.ReservationCreateRequest;
import com.exelent.booking.dto.reservation.ReservationFilterRequest;
import com.exelent.booking.dto.reservation.ReservationResponse;
import com.exelent.booking.dto.reservation.ReservationUpdateRequest;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.exception.ApiMessages;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.ReservationSpecifications;
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
    private final PricingService pricingService;
    private final ReservationAccessPolicy accessPolicy;

    @Transactional(readOnly = true)
    public PagedResponse<ReservationResponse> search(ReservationFilterRequest filter, Pageable pageable) {
        ReservationSortValidator.validate(pageable.getSort());

        User loggedIn = accessPolicy.currentUser();
        Long userId = accessPolicy.listScopeUserId(loggedIn);

        return PagedResponse.from(
                reservationRepository
                        .search(ReservationSpecifications.withFilters(
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
        User owner = accessPolicy.currentUser();
        BookableResource resource = resourceService.lockResource(request.resourceId());
        validateResourceAvailable(resource);
        accessPolicy.assertCanCreateWithStatus(owner, request.status());
        checkOverlap(resource.getId(), request.startTime(), request.endTime(), null);

        ReservationStatus status = request.status() == null ? ReservationStatus.PENDING : request.status();
        Reservation saved = reservationRepository.save(Reservation.builder()
                .user(owner)
                .resource(resource)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .status(status)
                .price(pricingService.resolvePrice(resource, request.startTime(), request.endTime(), request.price()))
                .build());
        return ReservationResponse.from(saved);
    }

    @Transactional
    public ReservationResponse update(Long id, ReservationUpdateRequest request) {
        Reservation reservation = getById(id);
        accessPolicy.requireOwnerOrAdmin(reservation);
        validateUpdatable(reservation);

        BookableResource resource = resourceService.lockResource(request.resourceId());
        validateResourceAvailable(resource);
        if (request.status() != ReservationStatus.CANCELLED) {
            checkOverlap(resource.getId(), request.startTime(), request.endTime(), reservation.getId());
        }
        return ReservationResponse.from(persistChanges(reservation, resource, request));
    }

    @Transactional
    public ReservationResponse cancel(Long id) {
        Reservation reservation = findOwnedOrAdmin(id);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, ApiMessages.ALREADY_CANCELLED);
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional
    public void delete(Long id) {
        reservationRepository.delete(getById(id));
    }

    private void validateUpdatable(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ApiException(HttpStatus.CONFLICT, ApiMessages.CANCELLED_CANNOT_UPDATE);
        }
    }

    private void validateResourceAvailable(BookableResource resource) {
        if (!resource.isAvailable()) {
            throw new ApiException(HttpStatus.CONFLICT, ApiMessages.RESOURCE_UNAVAILABLE);
        }
    }

    private Reservation persistChanges(Reservation reservation, BookableResource resource, ReservationUpdateRequest request) {
        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(pricingService.resolvePrice(resource, request.startTime(), request.endTime(), request.price()));
        reservation.setStatus(request.status());
        return reservationRepository.save(reservation);
    }

    private Reservation findOwnedOrAdmin(Long id) {
        Reservation reservation = getById(id);
        accessPolicy.requireOwnerOrAdmin(reservation);
        return reservation;
    }

    private Reservation getById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiMessages.RESERVATION_NOT_FOUND));
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
            throw new ApiException(HttpStatus.CONFLICT, ApiMessages.SLOT_ALREADY_BOOKED);
        }
    }
}
