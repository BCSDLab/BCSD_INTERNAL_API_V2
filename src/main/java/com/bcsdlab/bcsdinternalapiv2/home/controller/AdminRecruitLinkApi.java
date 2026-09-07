package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.RecruitLinkUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminRecruitLinkResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.RecruitLinkHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 모집 링크 API")
@SecurityRequirement(name = "JWT")
public interface AdminRecruitLinkApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "아직 저장된 적 없으면 본문이 null이다"),
    })
    @Operation(summary = "현재 모집 링크 설정 조회")
    AdminRecruitLinkResponse getCurrent();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "모집 링크 저장", description = "googleFormUrl은 forms.gle 또는 docs.google.com/forms만 허용한다(AC-10.6). 저장 시 이력을 남긴다(AC-10.7).")
    AdminRecruitLinkResponse update(@RequestBody @Valid RecruitLinkUpdateRequest request,
                                     @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {@ApiResponse(responseCode = "200")})
    @Operation(summary = "변경 이력 조회", description = "최신순.")
    List<RecruitLinkHistoryResponse> getHistory();
}
