package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.InitialSetupRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<MemberResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(memberService.getMe(memberId(jwt)));
    }

    @Override
    @GetMapping("/initial-setup")
    public ResponseEntity<MemberResponse> getInitialSetupInfo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(memberService.getInitialSetupInfo(memberId(jwt), passwordVersion(jwt)));
    }

    @Override
    @PostMapping("/initial-setup")
    public ResponseEntity<LoginResponse> completeInitialSetup(@AuthenticationPrincipal Jwt jwt,
                                                               @Valid @RequestBody InitialSetupRequest request,
                                                               HttpServletRequest servletRequest,
                                                               HttpServletResponse servletResponse) {
        return ResponseEntity.ok(memberService.completeInitialSetup(
                memberId(jwt), passwordVersion(jwt), request, servletRequest, servletResponse));
    }

    private Long memberId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }

    private long passwordVersion(Jwt jwt) {
        Number version = jwt.getClaim("pwv");
        return version != null ? version.longValue() : 0L;
    }
}
