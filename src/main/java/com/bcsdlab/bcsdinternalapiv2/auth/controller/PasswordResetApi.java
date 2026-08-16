package com.bcsdlab.bcsdinternalapiv2.auth.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetConfirmRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetRequestRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.request.PasswordResetTokenValidateRequest;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.ResetTokenValidationResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.SimpleMessageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "비밀번호 재설정 API")
public interface PasswordResetApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "429", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "비밀번호 재설정 이메일 요청",
            description = "가입된 이메일 존재 여부와 무관하게 항상 동일한 응답을 반환합니다.")
    @PostMapping("/reset-requests")
    ResponseEntity<SimpleMessageResponse> requestReset(
            @RequestBody @Valid PasswordResetRequestRequest request,
            @Parameter(hidden = true) HttpServletRequest servletRequest
    );

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "재설정 토큰 검증",
            description = "재설정 링크의 토큰이 유효한지 확인합니다. 토큰은 접근 로그에 남지 않도록 요청 본문으로 전달합니다.")
    @PostMapping("/reset-requests/validate")
    ResponseEntity<ResetTokenValidationResponse> validateResetToken(
            @RequestBody @Valid PasswordResetTokenValidateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "비밀번호 재설정 확정")
    @PostMapping("/reset")
    ResponseEntity<Void> confirmReset(@RequestBody @Valid PasswordResetConfirmRequest request);
}
