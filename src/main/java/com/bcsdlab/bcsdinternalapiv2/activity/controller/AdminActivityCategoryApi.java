package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCategoryUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityCategoryResponse;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
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

@Tag(name = "관리자 - 활동 카테고리 API")
@SecurityRequirement(name = "JWT")
public interface AdminActivityCategoryApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 카테고리 목록", description = "숨김 포함 전체, display_order 순.")
    List<AdminActivityCategoryResponse> getCategories();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 카테고리 생성")
    ResponseEntity<AdminActivityCategoryResponse> createCategory(
            @RequestBody @Valid ActivityCategoryCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "이름·헤드라인·히어로 이미지 수정", description = "slug는 불변이다.")
    AdminActivityCategoryResponse updateCategory(@PathVariable Long id,
                                                  @RequestBody @Valid ActivityCategoryUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 카테고리 삭제", description = "soft delete. 활동이 남아 있으면 409다(AC-3.8).")
    ResponseEntity<Void> deleteCategory(@PathVariable Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "공개/숨김")
    ResponseEntity<Void> publish(@PathVariable Long id, @RequestBody @Valid PublishRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "탭 순서 변경")
    ResponseEntity<Void> reorder(@RequestBody @Valid OrderRequest request);
}
