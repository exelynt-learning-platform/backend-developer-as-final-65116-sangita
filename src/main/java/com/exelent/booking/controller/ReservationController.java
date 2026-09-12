package com.exelent.booking.controller;

import com.exelent.booking.dto.PagedResponse;
import com.exelent.booking.dto.reservation.ReservationCreateRequest;
import com.exelent.booking.dto.reservation.ReservationFilterRequest;
import com.exelent.booking.dto.reservation.ReservationResponse;
import com.exelent.booking.dto.reservation.ReservationSort;
import com.exelent.booking.dto.reservation.ReservationUpdateRequest;
import com.exelent.booking.service.ReservationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public PagedResponse<ReservationResponse> getAll(
            @ModelAttribute ReservationFilterRequest filter,
            @PageableDefault(size = 10, sort = ReservationSort.DEFAULT, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        filter.validate();
        ReservationSort.validate(pageable.getSort());
        return reservationService.search(filter, pageable);
    }

    @GetMapping("/{id}")
    public ReservationResponse getOne(@PathVariable Long id) {
        return reservationService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody ReservationCreateRequest request) {
        ReservationResponse created = reservationService.create(request);
        return ResponseEntity.created(URI.create("/api/reservations/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ReservationResponse update(@PathVariable Long id, @Valid @RequestBody ReservationUpdateRequest request) {
        return reservationService.update(id, request);
    }

    @PostMapping("/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return reservationService.cancel(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
