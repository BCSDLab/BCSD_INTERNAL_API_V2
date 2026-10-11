package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesNotificationRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesNotificationResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/dues/semesters/{semesterId}/notifications")
@RequiredArgsConstructor
public class AdminDuesNotificationController implements AdminDuesNotificationApi {

    private final DuesNotificationService notificationService;

    @Override
    @PostMapping
    public ResponseEntity<DuesNotificationResponse> send(
            @PathVariable String semesterId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody DuesNotificationRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationService.send(semesterId, request, idempotencyKey,
                Long.valueOf(jwt.getSubject())));
    }
}
