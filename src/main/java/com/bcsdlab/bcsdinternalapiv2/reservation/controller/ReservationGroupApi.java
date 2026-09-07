package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "동아리방 예약 API")
public interface ReservationGroupApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "반복 예약 그룹 전체 취소", description = "본인 소유 그룹의 미래 회차를 모두 취소합니다. 이미 지난 회차는 그대로 둡니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{groupId}")
    ResponseEntity<Void> cancelReservationGroup(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, @PathVariable Long groupId);
}
