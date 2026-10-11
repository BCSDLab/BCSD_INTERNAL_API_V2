package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.EvidencePresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidencePresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.EvidenceResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 장부 증빙 API")
public interface AdminLedgerEvidenceApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "증빙 업로드 URL 발급", description = "PNG·JPG·WEBP·PDF, 10MB 이하만 받습니다. 확장자와 형식이 맞아야 합니다. "
            + "업로드 URL에는 형식과 크기가 서명돼 있어 다른 파일은 S3가 거부합니다. 업로드한 뒤 complete를 부르세요.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/presigned-url")
    ResponseEntity<EvidencePresignedUrlResponse> issuePresignedUrl(
            @RequestBody @Valid EvidencePresignedUrlRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "502", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "증빙 업로드 완료", description = "업로드된 파일이 신청한 형식·크기와 같은지 확인하고 확정합니다. "
            + "이미 확정된 증빙이면 그대로 돌려줍니다. 확정한 증빙은 장부 수정의 evidenceIds로 붙입니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{id}/complete")
    ResponseEntity<EvidenceResponse> complete(@PathVariable Long id);
}
