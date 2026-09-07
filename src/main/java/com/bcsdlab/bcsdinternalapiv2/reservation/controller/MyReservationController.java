package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MonthlyOccupancyResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MyReservationResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.service.ReservationService;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reservations/me")
@RequiredArgsConstructor
public class MyReservationController implements MyReservationApi {

    private final ReservationService reservationService;

    @Override
    @GetMapping("/monthly-occupancy")
    public ResponseEntity<MonthlyOccupancyResponse> getMyMonthlyOccupancy(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(reservationService.getMyMonthlyOccupancy(memberId(jwt), month));
    }

    @Override
    @GetMapping
    public ResponseEntity<MyReservationResponse> getMyReservations(@AuthenticationPrincipal Jwt jwt,
                                                                     @RequestParam(defaultValue = "upcoming") String status) {
        return ResponseEntity.ok(reservationService.getMyReservations(memberId(jwt), status));
    }

    private Long memberId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
