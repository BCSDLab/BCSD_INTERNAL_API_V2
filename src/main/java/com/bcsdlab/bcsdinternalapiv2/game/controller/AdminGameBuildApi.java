package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameBuildCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameBuildResponse;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@Tag(name = "관리자 - 게임 빌드 API")
@SecurityRequirement(name = "JWT")
public interface AdminGameBuildApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "빌드 메타 목록", description = "버전·상태·업로드 일시, 최신순.")
    List<AdminGameBuildResponse> getBuilds(Long gameId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "빌드 메타 등록",
            description = "1차: 버전만 등록하고 status=PENDING으로 시작한다. "
                    + "실제 ZIP 업로드·압축해제·서빙은 범위 밖이다(ADR-023, T-41).")
    ResponseEntity<AdminGameBuildResponse> createBuild(Long gameId, @Valid GameBuildCreateRequest request,
                                                        @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "빌드 메타 삭제")
    ResponseEntity<Void> deleteBuild(Long gameId, Long buildId);
}
