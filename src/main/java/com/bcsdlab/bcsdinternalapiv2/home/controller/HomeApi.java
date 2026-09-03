package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.HomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "메인 화면 API (홈페이지 공개)")
public interface HomeApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "메인 화면 위젯", description = "멘토 캐러셀 + Q&A + 모집 링크를 한 번에 반환합니다.")
    @GetMapping
    HomeResponse getHome();
}
