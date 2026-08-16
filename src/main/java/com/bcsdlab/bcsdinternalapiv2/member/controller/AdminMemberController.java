package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.AdminMemberCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.AdminMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/members")
@RequiredArgsConstructor
public class AdminMemberController implements AdminMemberApi {

    private final AdminMemberService adminMemberService;

    @Override
    @PostMapping
    public ResponseEntity<AdminMemberCreateResponse> createMember(@Valid @RequestBody AdminMemberCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminMemberService.createMember(request));
    }

    @Override
    @PostMapping("/{memberId}/resend-welcome-mail")
    public ResponseEntity<Void> resendWelcomeMail(@PathVariable Long memberId) {
        adminMemberService.resendWelcomeMail(memberId);
        return ResponseEntity.noContent().build();
    }
}
