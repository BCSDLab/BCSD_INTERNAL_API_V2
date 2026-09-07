package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.QnaUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminQnaResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - Q&A API")
@SecurityRequirement(name = "JWT")
public interface AdminQnaApi {

    @ApiResponses(value = {@ApiResponse(responseCode = "200")})
    @Operation(summary = "질문 목록", description = "숨김 포함 전체, display_order 순.")
    List<AdminQnaResponse> getQnaItems();

    @ApiResponses(value = {@ApiResponse(responseCode = "201")})
    @Operation(summary = "질문 추가")
    ResponseEntity<AdminQnaResponse> createQnaItem(@RequestBody @Valid QnaCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "질문 수정")
    AdminQnaResponse updateQnaItem(@PathVariable Long id, @RequestBody @Valid QnaUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "질문 삭제")
    ResponseEntity<Void> deleteQnaItem(@PathVariable Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "공개/숨김")
    ResponseEntity<Void> publish(@PathVariable Long id, @RequestBody @Valid PublishRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "순서 변경")
    ResponseEntity<Void> reorder(@RequestBody @Valid OrderRequest request);
}
