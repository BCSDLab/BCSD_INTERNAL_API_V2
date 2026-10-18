package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.request.ReservationCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.DailyReservationResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MonthlyOccupancyResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.service.ReservationService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reservations")
@RequiredArgsConstructor
public class ReservationController implements ReservationApi {

    private final ReservationService reservationService;

    @Override
    @GetMapping("/monthly-occupancy")
    public ResponseEntity<MonthlyOccupancyResponse> getMonthlyOccupancy(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(reservationService.getMonthlyOccupancy(month));
    }

    @Override
    @GetMapping("/daily")
    public ResponseEntity<DailyReservationResponse> getDaily(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @LoginMemberId(required = false) Long viewerMemberId) {
        return ResponseEntity.ok(reservationService.getDaily(date, viewerMemberId));
    }

    @Override
    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(@LoginMemberId Long loginMemberId,
                                                                        @Valid @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.ok(reservationService.create(loginMemberId, request));
    }

    @Override
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(@LoginMemberId Long loginMemberId,
                                                                           @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getDetail(loginMemberId, reservationId));
    }

    @Override
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@LoginMemberId Long loginMemberId, @PathVariable Long reservationId) {
        reservationService.cancel(loginMemberId, reservationId);
        return ResponseEntity.noContent().build();
    }
}
