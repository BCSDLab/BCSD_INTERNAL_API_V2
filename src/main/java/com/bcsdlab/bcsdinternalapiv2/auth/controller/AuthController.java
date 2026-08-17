package com.bcsdlab.bcsdinternalapiv2.auth.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.LoginRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.TokenResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                                HttpServletRequest servletRequest,
                                                HttpServletResponse servletResponse) {
        return ResponseEntity.ok(authService.login(request, servletRequest, servletResponse));
    }

    @Override
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(HttpServletRequest servletRequest,
                                                  HttpServletResponse servletResponse) {
        return ResponseEntity.ok(authService.reissue(servletRequest, servletResponse));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        authService.logout(servletRequest, servletResponse);
        return ResponseEntity.noContent().build();
    }
}
