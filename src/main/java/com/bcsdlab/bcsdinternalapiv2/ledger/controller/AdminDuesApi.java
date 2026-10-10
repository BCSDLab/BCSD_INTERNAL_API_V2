package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.SemesterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterCreatableResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.SemesterDuesSummaryResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 회비 학기 API")
public interface AdminDuesApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학기 회비 목록", description = "학기별 요약을 최신 학기부터 돌려줍니다. 집계는 조회할 때 계산합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<SemesterDuesListResponse> getSemesters();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학기 회비 생성 가능 여부", description = "오늘(Asia/Seoul) 날짜로 현재 학기를 정합니다. "
            + "만들 학기는 가장 최근 학기의 다음 학기(학기가 없으면 현재 학기)이고, 현재 학기의 바로 다음 학기까지만 만들 수 있습니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/creatable")
    ResponseEntity<SemesterCreatableResponse> getCreatable();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학기 회비 생성", description = "만들 수 있는 학기(생성 가능 여부의 nextSemester)만 만듭니다. "
            + "생성 시점에 활동 중이고 탈퇴하지 않은 회원을 명단으로 스냅샷하고, 납부 대상 여부는 인명부의 납부 여부를 따릅니다. "
            + "월 회비는 1~1,000,000원입니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<SemesterDuesSummaryResponse> createSemester(@RequestBody @Valid SemesterCreateRequest request,
                                                                @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학기 회원별 회비", description = "학기 요약과 명단 회원별 회비를 이름·학번 순으로 돌려줍니다. "
            + "학기 ID는 \"2026-2\" 형식입니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{semesterId}/members")
    ResponseEntity<SemesterDuesDetailResponse> getSemesterMembers(@PathVariable String semesterId);
}
