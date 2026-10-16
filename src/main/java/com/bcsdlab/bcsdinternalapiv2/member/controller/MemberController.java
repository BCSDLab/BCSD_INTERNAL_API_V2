package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.InitialSetupRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberContactUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PasswordChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoPresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoUrlUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PhotoPresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/members/me")
@RequiredArgsConstructor
public class MemberController implements MemberApi {

    private final MemberService memberService;

    @Override
    @GetMapping
    public ResponseEntity<MemberResponse> getMe(@LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(memberService.getMe(loginMemberId));
    }

    @Override
    @GetMapping("/initial-setup")
    public ResponseEntity<MemberResponse> getInitialSetupInfo(@LoginMemberId Long loginMemberId,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(memberService.getInitialSetupInfo(loginMemberId, passwordVersion(jwt)));
    }

    @Override
    @PostMapping("/initial-setup")
    public ResponseEntity<LoginResponse> completeInitialSetup(@LoginMemberId Long loginMemberId,
                                                               @AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody InitialSetupRequest request,
                                                               HttpServletRequest servletRequest,
                                                               HttpServletResponse servletResponse) {
        return ResponseEntity.ok(memberService.completeInitialSetup(
                loginMemberId, passwordVersion(jwt), request, servletRequest, servletResponse));
    }

    @Override
    @PatchMapping
    public ResponseEntity<Void> updateContact(@LoginMemberId Long loginMemberId,
                                               @Valid @RequestBody MemberContactUpdateRequest request) {
        memberService.updateContact(loginMemberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@LoginMemberId Long loginMemberId,
                                                @Valid @RequestBody PasswordChangeRequest request) {
        memberService.changePassword(loginMemberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/photo/presigned-url")
    public ResponseEntity<PhotoPresignedUrlResponse> issuePhotoPresignedUrl(
            @LoginMemberId Long loginMemberId, @Valid @RequestBody PhotoPresignedUrlRequest request) {
        return ResponseEntity.ok(memberService.issuePhotoPresignedUrl(loginMemberId, request));
    }

    @Override
    @PatchMapping("/photo")
    public ResponseEntity<Void> updatePhotoUrl(@LoginMemberId Long loginMemberId,
                                                @Valid @RequestBody PhotoUrlUpdateRequest request) {
        memberService.updatePhotoUrl(loginMemberId, request.photoUrl());
        return ResponseEntity.noContent().build();
    }

    private long passwordVersion(Jwt jwt) {
        Number version = jwt.getClaim("pwv");
        return version != null ? version.longValue() : 0L;
    }
}
