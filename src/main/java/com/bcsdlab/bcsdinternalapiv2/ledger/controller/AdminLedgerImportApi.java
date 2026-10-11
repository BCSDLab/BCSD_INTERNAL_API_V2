package com.bcsdlab.bcsdinternalapiv2.ledger.controller;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ImportCommitRequest;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportCommitResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ImportPreviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "관리자 - 장부 가져오기 API")
public interface AdminLedgerImportApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "413", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "거래내역 미리보기", description = "신한은행에서 내려받은 거래내역 .xlsx(1MB 이하)를 multipart 파트 file로 받아 "
            + "파싱만 하고 저장하지 않습니다. 이미 장부에 있는 거래는 빠집니다. 파트의 Content-Type은 보지 않고 확장자와 "
            + "xlsx 구조로 판단합니다. 최근 학기 명단에 이름이 한 명만 같은 회원이 있으면 그 회원과 DUES를 추천합니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ImportPreviewResponse> preview(
            @Parameter(description = "신한은행 거래내역 .xlsx", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
            @RequestPart("file") MultipartFile file);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "거래내역 반영", description = "미리보기의 모든 거래(1~5,000건)를 장부에 넣습니다. 서버가 rowKey를 다시 계산해 "
            + "다르면 400입니다. 이미 있는 거래는 건너뛰고 skippedDuplicateCount로 셉니다. 전부 반영하거나 전부 롤백합니다. "
            + "장부 잔액(balance)은 은행 잔액(bankBalance) 그대로입니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<ImportCommitResponse> commit(@RequestBody @Valid ImportCommitRequest request,
                                                @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);
}
