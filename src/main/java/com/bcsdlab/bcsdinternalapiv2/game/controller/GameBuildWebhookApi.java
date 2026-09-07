package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildWebhookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "게임 빌드 완료 웹훅 (홈페이지 서버 전용)")
public interface GameBuildWebhookApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "빌드 처리 결과 통지", description = "X-Game-Build-Secret 헤더로 인증한다(ADR-024).")
    ResponseEntity<Void> receiveResult(Long buildId, String secret, @Valid GameBuildWebhookRequest request);
}
