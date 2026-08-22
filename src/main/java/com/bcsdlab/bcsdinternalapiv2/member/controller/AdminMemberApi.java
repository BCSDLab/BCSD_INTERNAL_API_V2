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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "관리자 - 회원 API")
public interface AdminMemberApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "계정 생성", description = "학번이 로그인 아이디가 되며, 초기 비밀번호는 발급 후 이메일로 안내됩니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping
    ResponseEntity<AdminMemberCreateResponse> createMember(@RequestBody @Valid AdminMemberCreateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "환영 메일 재발송", description = "새 임시 비밀번호를 발급하고 계정 생성 메일을 다시 보냅니다. "
            + "이미 초기 설정을 완료한 계정에는 사용할 수 없습니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{memberId}/resend-welcome-mail")
    ResponseEntity<Void> resendWelcomeMail(@PathVariable Long memberId);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "인명부 조회", description = "검색어, 활동 여부, 학적 상태, 트랙, 구분으로 필터링하고 정렬·페이지네이션된 회원 목록과 "
            + "사이드바 집계 카운트를 반환합니다.")
    @SecurityRequirement(name = "JWT")
    @GetMapping
    ResponseEntity<MemberDirectoryResponse> getDirectory(@ModelAttribute MemberDirectoryQuery query,
                                                          @Parameter(hidden = true) Pageable pageable);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "학적 상태 변경", description = "회원의 학적 상태(재학/휴학/군휴학/졸업)를 변경합니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}/academic-status")
    ResponseEntity<Void> updateAcademicStatus(@PathVariable Long memberId,
                                               @RequestBody @Valid AcademicStatusUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "활동 여부 변경", description = "회원의 동아리 활동 여부를 변경합니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}/active")
    ResponseEntity<Void> updateActive(@PathVariable Long memberId, @RequestBody @Valid ActiveUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "회원 프로필 수정", description = "이름/트랙/기수/구분/학교/학부/역할/생일/납부여부/이메일/전화번호/깃허브 아이디를 수정합니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}")
    ResponseEntity<Void> updateProfile(@PathVariable Long memberId,
                                        @RequestBody @Valid AdminMemberProfileUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "권한 변경", description = "회원의 권한(관리자/일반)을 변경합니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}/role")
    ResponseEntity<Void> updateRole(@PathVariable Long memberId, @RequestBody @Valid RoleUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "탈퇴 상태 변경", description = "회원을 탈퇴(WITHDRAWN) 처리하거나 복구합니다. 실제 데이터는 삭제되지 않습니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}/withdrawal")
    ResponseEntity<Void> updateWithdrawal(@PathVariable Long memberId,
                                           @RequestBody @Valid WithdrawalUpdateRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "사진 업로드 URL 발급", description = "S3 presigned PUT URL을 발급합니다. "
            + "브라우저가 반환된 uploadUrl로 S3에 직접 PUT한 뒤, publicUrl을 프로필 사진 등록 API로 저장해야 합니다. "
            + "서버는 이미지 바이트를 경유하지 않습니다.")
    @SecurityRequirement(name = "JWT")
    @PostMapping("/{memberId}/photo/presigned-url")
    ResponseEntity<PhotoPresignedUrlResponse> issuePhotoPresignedUrl(
            @PathVariable Long memberId, @RequestBody @Valid PhotoPresignedUrlRequest request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "204"),
            @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "401", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "403", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(hidden = true))),
    })
    @Operation(summary = "프로필 사진 등록", description = "S3에 직접 PUT을 마친 뒤, 그 결과 publicUrl을 회원 프로필에 저장합니다.")
    @SecurityRequirement(name = "JWT")
    @PatchMapping("/{memberId}/photo")
    ResponseEntity<Void> updatePhotoUrl(@PathVariable Long memberId, @RequestBody @Valid PhotoUrlUpdateRequest request);
}
