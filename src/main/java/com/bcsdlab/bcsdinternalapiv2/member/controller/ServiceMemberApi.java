package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.ServiceMemberListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "서비스 API", description = "내부 서비스(bbot) 전용. 사용자 JWT가 아니라 서비스 토큰(Bearer)으로 인증한다.")
public interface ServiceMemberApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "회원 명부 조회(bbot)", description = "탈퇴하지 않은 회원의 id·이름·Slack ID·GitHub ID·트랙 코드·구분·"
            + "활동 여부·학적 상태를 id 순으로 반환합니다. 이메일·전화번호·학번 등 개인정보는 포함하지 않습니다.")
    @GetMapping
    ResponseEntity<ServiceMemberListResponse> getMembers();
}
