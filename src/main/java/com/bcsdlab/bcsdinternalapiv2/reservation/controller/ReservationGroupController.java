package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Void> cancelReservationGroup(@LoginMemberId Long loginMemberId, @PathVariable Long groupId) {
        reservationService.cancelGroup(loginMemberId, groupId);
        return ResponseEntity.noContent().build();
    }
}
