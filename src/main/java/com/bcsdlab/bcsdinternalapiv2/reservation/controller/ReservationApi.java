package com.bcsdlab.bcsdinternalapiv2.reservation.controller;

import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.request.ReservationCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.DailyReservationResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MonthlyOccupancyResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "동아리방 예약 API")
public interface ReservationApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "월별 예약 현황", description = "해당 월의 날짜별 예약된 시간(분)을 반환합니다. 로그인 여부와 무관하게 조회 가능합니다.")
    @GetMapping("/monthly-occupancy")
    ResponseEntity<MonthlyOccupancyResponse> getMonthlyOccupancy(
            @Parameter(example = "2026-09") @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
    })
    @Operation(summary = "일별 예약 목록", description = "해당 날짜의 예약 목록을 반환합니다. 비로그인 상태에서는 예약자 이름과 목적이 응답에서 생략됩니다.")
    @GetMapping("/daily")
    ResponseEntity<DailyReservationResponse> getDaily(
            @Parameter(example = "2026-09-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "예약 생성", description = "요청에 repeat 필드가 있으면 반복 예약으로 생성합니다. "
            + "반복 예약은 겹치거나 하루 한도를 초과하는 회차만 건너뛰고, 단건 예약은 충돌 시 요청 전체가 실패합니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<ReservationCreateResponse> createReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid ReservationCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "예약 상세 조회", description = "본인 예약만 조회할 수 있습니다. 반복 예약이면 그룹의 전체 회차 목록을 포함합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/{reservationId}")
    ResponseEntity<ReservationDetailResponse> getReservationDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, @PathVariable Long reservationId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "예약 취소", description = "본인 예약 1건(반복 예약이면 해당 회차 1건)만 취소합니다. 이미 시작된 예약은 취소할 수 없습니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{reservationId}")
    ResponseEntity<Void> cancelReservation(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt, @PathVariable Long reservationId);
}
