package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberDirectoryQuery;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberDirectoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Tag(name = "회원 API")
public interface MemberDirectoryApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "인명부 조회", description = "검색어, 활동 여부, 학적 상태, 트랙, 구분으로 필터링하고 정렬·페이지네이션된 회원 목록과 "
            + "사이드바 집계 카운트를 반환합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<MemberDirectoryResponse> getDirectory(@ModelAttribute MemberDirectoryQuery query,
                                                          @Parameter(hidden = true) Pageable pageable);
}
