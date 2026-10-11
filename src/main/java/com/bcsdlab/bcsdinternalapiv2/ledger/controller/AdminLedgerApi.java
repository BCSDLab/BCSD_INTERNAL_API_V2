package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.DuesLinkRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.LedgerEntryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.LedgerEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 장부 API")
public interface AdminLedgerApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "장부 내역 목록", description = "전체 내역을 최신 거래부터 돌려줍니다. 쿼리 파라미터(필터)는 무시합니다. "
            + "balance는 은행 거래 후 잔액이고, linkStatus는 연결이 있으면 CONFIRMED, 없으면 회비(DUES) 분류일 때 PENDING, "
            + "그 밖에는 NONE입니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<LedgerEntryListResponse> getEntries();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "장부 내역 수정", description = "거래 상대·분류·내용·비고·증빙을 통째로 바꿉니다. 금액·시각·입출금 구분은 바꿀 수 없습니다. "
            + "분류가 바뀌면 회비 연결이 해제됩니다. 증빙 업로드가 생기기 전이라 evidenceIds는 빈 배열만 받습니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{id}")
    ResponseEntity<LedgerEntryResponse> updateEntry(@PathVariable Long id,
                                                    @RequestBody @Valid LedgerEntryUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "회비 연결", description = "장부 기록을 학기·회원의 회비에 연결합니다. 이미 연결돼 있으면 새 대상으로 옮깁니다. "
            + "연결하면 분류는 입금·출금 모두 회비(DUES)가 됩니다. 명단에 없는 회원이면 404, 납부 비대상 회원이면 409입니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/{id}/dues-link")
    ResponseEntity<LedgerEntryResponse> linkEntry(@PathVariable Long id, @RequestBody @Valid DuesLinkRequest request,
                                                  @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "회비 연결 해제", description = "연결만 끊고 분류는 그대로 둡니다. 연결이 없어도 204입니다.")
    @SecurityRequirement(name = "JWT")
    @DeleteMapping("/{id}/dues-link")
    ResponseEntity<Void> unlinkEntry(@PathVariable Long id);
}
