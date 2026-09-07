package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.MemberVisibilityRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageMemberResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "관리자 트랙 멤버 API")
public interface AdminTrackPageMemberApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 트랙 페이지"),
    })
    @Operation(summary = "트랙에 배정된 부원 목록", description = "명부 join(이름·등급·프로필 사진). 부원 검색은 GET /members?name=를 그대로 쓴다.")
    List<AdminTrackPageMemberResponse> getMembers(Long trackPageId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 트랙 페이지 또는 부원"),
            @ApiResponse(responseCode = "409", description = "이미 배정된 부원(INV-8)"),
    })
    @Operation(summary = "부원 배정")
    List<AdminTrackPageMemberResponse> attachMembers(Long trackPageId,
                                                       @Valid TrackPageMembersAttachRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", description = "배정되지 않은 부원"),
    })
    @Operation(summary = "배정 해제")
    ResponseEntity<Void> detachMember(Long trackPageId, Long memberId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "404", description = "배정되지 않은 부원"),
    })
    @Operation(summary = "숨김/공개 전환")
    ResponseEntity<Void> updateVisibility(Long trackPageId, Long memberId,
                                           @Valid MemberVisibilityRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", description = "id 집합 불일치(INV-4)"),
    })
    @Operation(summary = "순서 변경")
    ResponseEntity<Void> reorder(Long trackPageId, @Valid OrderRequest request);
}
