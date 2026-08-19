package com.bcsdlab.bcsdinternalapiv2.curriculum.controller;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicDetailsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.TopicRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumTopicResponse;
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

@Tag(name = "관리자 - 커리큘럼 토픽 API")
@SecurityRequirement(name = "JWT")
public interface AdminTopicApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "토픽 제목 수정")
    CurriculumTopicResponse updateTopic(@PathVariable Long id, @RequestBody @Valid TopicRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "토픽 삭제", description = "하위 세부항목이 cascade로 함께 삭제된다.")
    ResponseEntity<Void> deleteTopic(@PathVariable Long id);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "세부 항목 전체 교체", description = "빈 배열이면 전부 삭제된다(AC-2.9, 에러가 아니다).")
    List<String> replaceDetails(@PathVariable Long id, @RequestBody @Valid TopicDetailsReplaceRequest request);
}
