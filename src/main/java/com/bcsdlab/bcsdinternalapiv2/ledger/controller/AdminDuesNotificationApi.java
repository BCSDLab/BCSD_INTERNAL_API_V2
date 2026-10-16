package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesNotificationRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.DuesNotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "관리자 - 회비 Slack 알림 API")
public interface AdminDuesNotificationApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "회비 Slack 알림 발송", description = "회원마다 받은 본문을 봇 DM으로 보냅니다(최대 50명, 본문 4,000자). "
            + "본문의 {담당자 멘션}은 로그인한 관리자의 Slack 멘션으로 바뀌고, http(s) 링크 외의 <, >, &는 글자로 바뀝니다. "
            + "Idempotency-Key가 필수이며, 같은 키로 다시 보내면 이미 보낸 회원은 건너뜁니다. 부분 실패도 200입니다. "
            + "testToSelf가 true면 첫 번째 본문만 내 Slack으로 1건 보냅니다(요청 키 불필요).")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<DuesNotificationResponse> send(
            @PathVariable String semesterId,
            @Parameter(description = "발송 한 건마다 만드는 요청 키(UUID 권장, 8~64자). testToSelf면 무시합니다.")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid DuesNotificationRequest request,
            @LoginMemberId Long loginMemberId);
}
