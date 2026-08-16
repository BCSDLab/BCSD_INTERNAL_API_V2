package com.bcsdlab.bcsdinternalapiv2.auth.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.LoginRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "인증 API")
public interface AuthApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "423", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "로그인", description = "학번과 비밀번호로 로그인합니다. 최초 로그인 계정은 setup 스코프 토큰을 발급받습니다.")
    @PostMapping("/login")
    ResponseEntity<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            @Parameter(hidden = true) HttpServletRequest servletRequest,
            @Parameter(hidden = true) HttpServletResponse servletResponse
    );

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "액세스 토큰 재발급", description = "refreshToken 쿠키로 새 액세스 토큰을 발급합니다.")
    @PostMapping("/reissue")
    ResponseEntity<TokenResponse> reissue(
            @Parameter(hidden = true) HttpServletRequest servletRequest,
            @Parameter(hidden = true) HttpServletResponse servletResponse
    );

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
    })
    @Operation(summary = "로그아웃")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/logout")
    ResponseEntity<Void> logout(
            @Parameter(hidden = true) HttpServletRequest servletRequest,
            @Parameter(hidden = true) HttpServletResponse servletResponse
    );
}
