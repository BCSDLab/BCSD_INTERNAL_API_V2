package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MonthlyOccupancyResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MyReservationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "동아리방 예약 API")
public interface MyReservationApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "내 월별 예약 현황", description = "해당 월의 날짜별 내 예약 시간(분)을 반환합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/monthly-occupancy")
    ResponseEntity<MonthlyOccupancyResponse> getMyMonthlyOccupancy(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "2026-09") @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "내 예약 목록", description = "예정된(upcoming) 또는 지난(past) 내 예약 목록을 반환합니다. "
            + "반복 예약은 그룹당 대표 1건만 포함됩니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<MyReservationResponse> getMyReservations(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @Parameter(example = "upcoming") @RequestParam(defaultValue = "upcoming") String status);
}
