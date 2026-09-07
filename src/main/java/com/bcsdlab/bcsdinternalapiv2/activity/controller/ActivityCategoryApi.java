package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.ActivityCategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@Tag(name = "활동 API (홈페이지 공개)")
public interface ActivityCategoryApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "공개 활동 카테고리 목록", description = "탭 노출 순서(display_order)대로 공개된 카테고리만 반환합니다.")
    List<ActivityCategoryResponse> getCategories();
}
