package com.bcsdlab.bcsdinternalapiv2.track.controller;

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
                                                              @Valid @RequestBody TrackPageMembersAttachRequest request) {
        return adminTrackPageMemberService.attachMembers(trackPageId, request);
    }

    @Override
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> detachMember(@PathVariable Long trackPageId, @PathVariable Long memberId) {
        adminTrackPageMemberService.detachMember(trackPageId, memberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}/visibility")
    public ResponseEntity<Void> updateVisibility(@PathVariable Long trackPageId, @PathVariable Long memberId,
                                                  @Valid @RequestBody MemberVisibilityRequest request) {
        adminTrackPageMemberService.updateVisibility(trackPageId, memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@PathVariable Long trackPageId, @Valid @RequestBody OrderRequest request) {
        adminTrackPageMemberService.reorder(trackPageId, request);
        return ResponseEntity.noContent().build();
    }
}
