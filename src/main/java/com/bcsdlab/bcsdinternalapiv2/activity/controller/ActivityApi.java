package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityTimelineResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "활동 API (홈페이지 공개)")
public interface ActivityApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 숨겨진 카테고리"),
    })
    @Operation(summary = "연도 그룹 활동 타임라인",
            description = "연도 내림차순 → 월 내림차순 → display_order 순으로, 공개된 활동만 반환합니다.")
    List<ActivityTimelineResponse> getTimeline(String category);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 숨겨진 활동"),
    })
    @Operation(summary = "활동 상세")
    ActivityDetailResponse getActivity(Long id);
}
