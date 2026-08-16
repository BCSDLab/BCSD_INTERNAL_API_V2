package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.AdminMemberCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 회원 API")
public interface AdminMemberApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "계정 생성", description = "학번이 로그인 아이디가 되며, 초기 비밀번호는 발급 후 이메일로 안내됩니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<AdminMemberCreateResponse> createMember(@RequestBody @Valid AdminMemberCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "환영 메일 재발송", description = "새 임시 비밀번호를 발급하고 계정 생성 메일을 다시 보냅니다. "
            + "이미 초기 설정을 완료한 계정에는 사용할 수 없습니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{memberId}/resend-welcome-mail")
    ResponseEntity<Void> resendWelcomeMail(@PathVariable Long memberId);
}
