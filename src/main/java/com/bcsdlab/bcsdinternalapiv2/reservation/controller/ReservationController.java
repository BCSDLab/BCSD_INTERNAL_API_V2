package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(reservationService.getDaily(date, viewerMemberId(jwt)));
    }

    @Override
    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(@AuthenticationPrincipal Jwt jwt,
                                                                        @Valid @RequestBody ReservationCreateRequest request) {
        return ResponseEntity.ok(reservationService.create(memberId(jwt), request));
    }

    @Override
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDetailResponse> getReservationDetail(@AuthenticationPrincipal Jwt jwt,
                                                                           @PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.getDetail(memberId(jwt), reservationId));
    }

    @Override
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancelReservation(@AuthenticationPrincipal Jwt jwt, @PathVariable Long reservationId) {
        reservationService.cancel(memberId(jwt), reservationId);
        return ResponseEntity.noContent().build();
    }

    private Long memberId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private Long viewerMemberId(Jwt jwt) {
        return jwt == null ? null : Long.valueOf(jwt.getSubject());
    }
}
