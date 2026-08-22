package com.bcsdlab.bcsdinternalapiv2.member.controller;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AcademicStatusUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.ActiveUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberProfileUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberDirectoryQuery;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoPresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoUrlUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.RoleUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.WithdrawalUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.AdminMemberCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberDirectoryResponse;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PhotoPresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.AdminMemberService;
import com.bcsdlab.bcsdinternalapiv2.member.service.MemberDirectoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final MemberDirectoryService memberDirectoryService;

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

    @Override
    @GetMapping
    public ResponseEntity<MemberDirectoryResponse> getDirectory(
            @ModelAttribute MemberDirectoryQuery query,
            @PageableDefault(size = 8, sort = "generation", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(memberDirectoryService.getDirectory(query, pageable));
    }

    @Override
    @PatchMapping("/{memberId}/academic-status")
    public ResponseEntity<Void> updateAcademicStatus(@PathVariable Long memberId,
                                                      @Valid @RequestBody AcademicStatusUpdateRequest request) {
        memberDirectoryService.changeAcademicStatus(memberId, request.academicStatus());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}/active")
    public ResponseEntity<Void> updateActive(@PathVariable Long memberId,
                                              @Valid @RequestBody ActiveUpdateRequest request) {
        memberDirectoryService.changeActive(memberId, request.active());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}")
    public ResponseEntity<Void> updateProfile(@PathVariable Long memberId,
                                               @Valid @RequestBody AdminMemberProfileUpdateRequest request) {
        memberDirectoryService.updateProfile(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<Void> updateRole(@PathVariable Long memberId,
                                            @Valid @RequestBody RoleUpdateRequest request) {
        memberDirectoryService.changeRole(memberId, request.role());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{memberId}/withdrawal")
    public ResponseEntity<Void> updateWithdrawal(@PathVariable Long memberId,
                                                  @Valid @RequestBody WithdrawalUpdateRequest request) {
        memberDirectoryService.changeWithdrawal(memberId, request.withdrawn());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/{memberId}/photo/presigned-url")
    public ResponseEntity<PhotoPresignedUrlResponse> issuePhotoPresignedUrl(
            @PathVariable Long memberId, @Valid @RequestBody PhotoPresignedUrlRequest request) {
        return ResponseEntity.ok(memberDirectoryService.issuePhotoPresignedUrl(memberId, request));
    }

    @Override
    @PatchMapping("/{memberId}/photo")
    public ResponseEntity<Void> updatePhotoUrl(@PathVariable Long memberId,
                                                @Valid @RequestBody PhotoUrlUpdateRequest request) {
        memberDirectoryService.updatePhotoUrl(memberId, request.photoUrl());
        return ResponseEntity.noContent().build();
    }
}
