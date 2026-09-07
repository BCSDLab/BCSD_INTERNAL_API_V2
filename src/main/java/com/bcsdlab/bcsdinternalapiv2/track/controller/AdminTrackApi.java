package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackMasterResponse;
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

@Tag(name = "관리자 - 트랙 마스터 API")
@SecurityRequirement(name = "JWT")
public interface AdminTrackApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "트랙 마스터 목록", description = "부원 소속용 트랙 마스터. 홈페이지 프로필 유무를 함께 반환합니다.")
    List<TrackMasterResponse> getTracks();

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "트랙 마스터 생성")
    ResponseEntity<TrackMasterResponse> createTrack(@RequestBody @Valid TrackMasterCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "트랙 마스터 수정", description = "이름과 활성 여부만 수정할 수 있습니다. code는 불변입니다.")
    TrackMasterResponse updateTrack(@PathVariable Long id, @RequestBody @Valid TrackMasterUpdateRequest request);
}
