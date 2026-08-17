package com.bcsdlab.bcsdinternalapiv2.auth.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetConfirmRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetRequestRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetTokenValidateRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.ResetTokenValidationResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.SimpleMessageResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth/password")
@RequiredArgsConstructor
public class PasswordResetController implements PasswordResetApi {

    private static final String SENT_MESSAGE = "입력하신 이메일로 재설정 링크를 보냈습니다.";

    private final PasswordResetService passwordResetService;

    @Override
    @PostMapping("/reset-requests")
    public ResponseEntity<SimpleMessageResponse> requestReset(@Valid @RequestBody PasswordResetRequestRequest request,
                                                               HttpServletRequest servletRequest) {
        passwordResetService.requestReset(request.email(), servletRequest.getRemoteAddr());
        return ResponseEntity.ok(new SimpleMessageResponse(SENT_MESSAGE));
    }

    @Override
    @PostMapping("/reset-requests/validate")
    public ResponseEntity<ResetTokenValidationResponse> validateResetToken(
            @Valid @RequestBody PasswordResetTokenValidateRequest request) {
        return ResponseEntity.ok(passwordResetService.validateToken(request.token()));
    }

    @Override
    @PostMapping("/reset")
    public ResponseEntity<Void> confirmReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
        passwordResetService.confirmReset(request.token(), request.newPassword(), request.newPasswordConfirm());
        return ResponseEntity.noContent().build();
    }
}
