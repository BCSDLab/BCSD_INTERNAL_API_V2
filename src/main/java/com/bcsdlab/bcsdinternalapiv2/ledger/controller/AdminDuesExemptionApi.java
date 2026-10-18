package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ExemptionUpsertRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionReasonListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 회비 면제 API")
public interface AdminDuesExemptionApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "면제 목록", description = "전체 면제를 id 순으로 돌려줍니다. 면제는 학기에 묶이지 않는 월 범위입니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/exemptions")
    ResponseEntity<ExemptionListResponse> getExemptions();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "면제 추가", description = "겹치는 면제가 있어도 저장합니다(하나라도 걸린 달은 면제). "
            + "처음 쓰는 사유는 사유 목록에 함께 등록됩니다. 사유는 앞뒤 공백을 지우고 NFC로 맞춥니다. "
            + "종료 월이 시작 월보다 빠르면 400, 회원이 없으면 404입니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/exemptions")
    ResponseEntity<ExemptionResponse> createExemption(@RequestBody @Valid ExemptionUpsertRequest request,
                                                      @LoginMemberId Long loginMemberId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "면제 수정", description = "면제 전체를 바꿉니다. 면제나 회원이 없으면 404입니다.")
    @SecurityRequirement(name = "JWT")
    @PutMapping("/exemptions/{id}")
    ResponseEntity<ExemptionResponse> updateExemption(@PathVariable Long id,
                                                      @RequestBody @Valid ExemptionUpsertRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "면제 사유 목록", description = "기본 사유 15개 다음에 등록 순으로 돌려줍니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping("/exemption-reasons")
    ResponseEntity<ExemptionReasonListResponse> getReasons();
}
