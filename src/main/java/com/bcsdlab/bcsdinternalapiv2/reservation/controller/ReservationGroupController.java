package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/reservations/groups")
@RequiredArgsConstructor
public class ReservationGroupController implements ReservationGroupApi {

    private final ReservationService reservationService;

    @Override
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> cancelReservationGroup(@AuthenticationPrincipal Jwt jwt, @PathVariable Long groupId) {
        reservationService.cancelGroup(memberId(jwt), groupId);
        return ResponseEntity.noContent().build();
    }

    private Long memberId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}
