package com.exelent.booking.service;

import com.exelent.booking.domain.Reservation;
import com.exelent.booking.domain.ReservationStatus;
import com.exelent.booking.domain.BookableResource;
import com.exelent.booking.domain.Role;
import com.exelent.booking.domain.User;
import com.exelent.booking.dto.reservation.ReservationCreateRequest;
import com.exelent.booking.dto.reservation.ReservationResponse;
import com.exelent.booking.exception.ApiException;
import com.exelent.booking.repository.ReservationRepository;
import com.exelent.booking.repository.UserRepository;
import com.exelent.booking.security.AuthHelper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ResourceService resourceService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthHelper authHelper;

    @InjectMocks
    private ReservationService reservationService;

    private User user;
    private BookableResource resource;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        user = User.builder().id(10L).username("user").email("user@gmail.com").password("x").role(Role.USER).enabled(true).build();
        resource = BookableResource.builder()
                .id(5L)
                .name("Meeting Room A")
                .hourlyRate(new BigDecimal("50.00"))
                .available(true)
                .build();
        start = LocalDateTime.of(2026, 10, 1, 9, 0);
        end = start.plusHours(2);
    }

    @Test
    void createUsesJwtUserAndCalculatesPrice() {
        when(authHelper.getLoggedInUser()).thenReturn(user);
        when(userRepository.getReferenceById(10L)).thenReturn(user);
        when(resourceService.getResource(5L)).thenReturn(resource);
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), any(), anyLong())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });

        ReservationCreateRequest request = new ReservationCreateRequest(5L, start, end, null, null);
        ReservationResponse response = reservationService.create(request);

        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.username()).isEqualTo("user");
        assertThat(response.price()).isEqualByComparingTo("100.00");
        assertThat(response.status()).isEqualTo(ReservationStatus.PENDING);

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).save(captor.capture());
        assertThat(captor.getValue().getUser().getId()).isEqualTo(10L);
    }

    @Test
    void userCannotCreateConfirmedReservation() {
        when(authHelper.getLoggedInUser()).thenReturn(user);
        when(userRepository.getReferenceById(10L)).thenReturn(user);
        when(resourceService.getResource(5L)).thenReturn(resource);
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any(), any(), anyLong())).thenReturn(false);

        ReservationCreateRequest request = new ReservationCreateRequest(5L, start, end, new BigDecimal("20.00"), ReservationStatus.CONFIRMED);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void userCannotViewSomeoneElsesReservation() {
        User other = User.builder().id(11L).username("user2").email("user2@gmail.com").password("x").role(Role.USER).enabled(true).build();
        Reservation reservation = Reservation.builder()
                .id(7L)
                .user(other)
                .resource(resource)
                .startTime(start)
                .endTime(end)
                .status(ReservationStatus.PENDING)
                .price(new BigDecimal("50.00"))
                .build();
        when(reservationRepository.findById(7L)).thenReturn(java.util.Optional.of(reservation));
        when(authHelper.getLoggedInUser()).thenReturn(user);

        assertThatThrownBy(() -> reservationService.findById(7L))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void overlappingReservationIsRejected() {
        when(authHelper.getLoggedInUser()).thenReturn(user);
        when(userRepository.getReferenceById(10L)).thenReturn(user);
        when(resourceService.getResource(5L)).thenReturn(resource);
        when(reservationRepository.existsOverlappingReservation(eq(5L), any(), eq(start), eq(end), eq(-1L))).thenReturn(true);

        ReservationCreateRequest request = new ReservationCreateRequest(5L, start, end, new BigDecimal("10.00"), null);

        assertThatThrownBy(() -> reservationService.create(request))
                .isInstanceOf(ApiException.class)
                .extracting(ex -> ((ApiException) ex).getStatus())
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
