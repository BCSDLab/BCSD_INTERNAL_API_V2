package com.bcsdlab.bcsdinternalapiv2.home.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.MentorSlotCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminMentorSlotResponse;
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

@Tag(name = "관리자 - 멘토 슬롯 API")
@SecurityRequirement(name = "JWT")
public interface AdminMentorSlotApi {

    @ApiResponses(value = {@ApiResponse(responseCode = "200")})
    @Operation(summary = "노출 중 멘토 목록", description = "순서 포함.")
    List<AdminMentorSlotResponse> getSlots();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "멘토 추가", description = "이미 노출 중이면 409(INV-20, AC-10.1). 부원 검색은 GET /members?name=를 그대로 쓴다.")
    List<AdminMentorSlotResponse> addSlot(@RequestBody @Valid MentorSlotCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "멘토 제외")
    ResponseEntity<Void> removeSlot(@PathVariable Long memberId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "노출 순서 변경")
    ResponseEntity<Void> reorder(@RequestBody @Valid OrderRequest request);
}
