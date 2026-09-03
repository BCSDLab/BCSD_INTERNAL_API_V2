package com.bcsdlab.bcsdinternalapiv2.game.controller;

import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.request.GameMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.game.controller.dto.response.AdminGameMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "관리자 - 게임 참여 멤버 API")
public interface AdminGameMemberApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게임"),
    })
    @Operation(summary = "게임에 배정된 참여 멤버 목록", description = "명부 join(이름·등급·프로필 사진). 부원 검색은 GET /members?name=를 그대로 쓴다.")
    List<AdminGameMemberResponse> getMembers(Long gameId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 게임 또는 부원"),
            @ApiResponse(responseCode = "409", description = "이미 배정된 부원"),
    })
    @Operation(summary = "참여 멤버 배정")
    List<AdminGameMemberResponse> attachMembers(Long gameId, @Valid GameMembersAttachRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", description = "배정되지 않은 부원"),
    })
    @Operation(summary = "배정 해제")
    ResponseEntity<Void> detachMember(Long gameId, Long memberId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", description = "id 집합 불일치(INV-4)"),
    })
    @Operation(summary = "순서 변경")
    ResponseEntity<Void> reorder(Long gameId, @Valid OrderRequest request);
}
