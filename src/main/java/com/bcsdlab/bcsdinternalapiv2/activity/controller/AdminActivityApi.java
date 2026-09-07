package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityImagesReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivitySummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "관리자 - 활동 API")
@SecurityRequirement(name = "JWT")
public interface AdminActivityApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 목록", description = "숨김 포함. categoryId/year/published로 선택적으로 필터링한다.")
    Page<AdminActivitySummaryResponse> getActivities(@RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) Integer year,
                                                      @RequestParam(required = false) Boolean published,
                                                      Pageable pageable);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 상세 조회")
    AdminActivityDetailResponse getActivity(@PathVariable Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 생성", description = "본문은 저장 전 정제된다(ADR-008). 생성 직후 공개 상태다.")
    ResponseEntity<AdminActivityDetailResponse> createActivity(@RequestBody @Valid ActivityCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 수정")
    AdminActivityDetailResponse updateActivity(@PathVariable Long id,
                                                @RequestBody @Valid ActivityUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 삭제", description = "soft delete — 데이터는 보존된다.")
    ResponseEntity<Void> deleteActivity(@PathVariable Long id);

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
    @Operation(summary = "같은 (카테고리, 연, 월) 안에서 순서 변경")
    ResponseEntity<Void> reorder(@Parameter(required = true) @RequestParam Long categoryId,
                                 @Parameter(required = true) @RequestParam int year,
                                 @Parameter(required = true) @RequestParam int month,
                                 @RequestBody @Valid OrderRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 사진 전체 교체", description = "첫 번째 항목이 목록 썸네일이다(INV-12).")
    List<String> replaceImages(@PathVariable Long id, @RequestBody @Valid ActivityImagesReplaceRequest request);
}
