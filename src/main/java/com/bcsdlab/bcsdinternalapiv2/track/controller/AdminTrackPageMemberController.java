package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.MemberVisibilityRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.track.service.AdminTrackPageMemberService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/track-pages/{trackPageId}/members")
@RequiredArgsConstructor
public class AdminTrackPageMemberController implements AdminTrackPageMemberApi {

    private final AdminTrackPageMemberService adminTrackPageMemberService;

    @Override
    @GetMapping
    public List<AdminTrackPageMemberResponse> getMembers(@PathVariable Long trackPageId) {
        return adminTrackPageMemberService.getMembers(trackPageId);
    }

    @Override
    @PostMapping
    public List<AdminTrackPageMemberResponse> attachMembers(@PathVariable Long trackPageId,
                                                              @Valid @RequestBody TrackPageMembersAttachRequest request,
                                                              @LoginMemberId Long loginMemberId) {
        return adminTrackPageMemberService.attachMembers(trackPageId, request, loginMemberId);
    }

    @Override
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> detachMember(@PathVariable Long trackPageId, @PathVariable Long memberId,
                                              @LoginMemberId Long loginMemberId) {
        adminTrackPageMemberService.detachMember(trackPageId, memberId, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}/visibility")
    public ResponseEntity<Void> updateVisibility(@PathVariable Long trackPageId, @PathVariable Long memberId,
                                                  @Valid @RequestBody MemberVisibilityRequest request,
                                                  @LoginMemberId Long loginMemberId) {
        adminTrackPageMemberService.updateVisibility(trackPageId, memberId, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@PathVariable Long trackPageId, @Valid @RequestBody OrderRequest request,
                                         @LoginMemberId Long loginMemberId) {
        adminTrackPageMemberService.reorder(trackPageId, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }
}
