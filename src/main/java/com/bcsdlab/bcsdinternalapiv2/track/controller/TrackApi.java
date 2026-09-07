package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "트랙 API (홈페이지 공개)")
public interface TrackApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "공개 트랙 목록", description = "홈페이지 노출 순서(display_order)대로, 공개된 트랙만 반환합니다.")
    @GetMapping
    List<TrackSummaryResponse> getTracks();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "트랙 페이지 상세", description = "숨김 처리되었거나 존재하지 않는 트랙은 구분 없이 404를 반환합니다.")
    @GetMapping("/{slug}")
    TrackDetailResponse getTrack(@PathVariable String slug);
}
