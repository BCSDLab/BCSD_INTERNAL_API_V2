package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameSlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 게임 API")
@SecurityRequirement(name = "JWT")
public interface AdminGameApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "게임 목록", description = "숨김 포함 전체, display_order 순.")
    List<AdminGameSummaryResponse> getGames();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "게임 편집 화면 조회 (기본정보·설명 탭)")
    AdminGameDetailResponse getGame(@PathVariable Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "게임 생성", description = "slug는 name에서 자동 생성됩니다(AC-9.1). 생성 직후는 숨김 상태다.")
    ResponseEntity<AdminGameDetailResponse> createGame(@RequestBody @Valid GameCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "게임 기본정보·설명 수정", description = "description은 저장 시 jsoup으로 정제됩니다(ADR-008).")
    AdminGameDetailResponse updateGame(@PathVariable Long id, @RequestBody @Valid GameUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "slug 수동 변경", description = "리다이렉트를 지원하지 않습니다 — 변경 전 URL은 더 이상 유효하지 않습니다.")
    AdminGameDetailResponse changeSlug(@PathVariable Long id, @RequestBody @Valid GameSlugChangeRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "공개/숨김")
    ResponseEntity<Void> publish(@PathVariable Long id, @RequestBody @Valid PublishRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "랜딩 노출 순서 변경", description = "ids는 전체 게임 id 집합과 정확히 일치해야 합니다(AC-9.5).")
    ResponseEntity<Void> reorder(@RequestBody @Valid OrderRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "게임 삭제", description = "soft delete — 데이터는 보존됩니다(AC-9.11).")
    ResponseEntity<Void> deleteGame(@PathVariable Long id);
}
