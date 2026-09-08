package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.auth.controller.dto.response.LoginResponse;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthException;
import com.bcsdlab.bcsdinternalapiv2.auth.exception.AuthExceptionType;
import com.bcsdlab.bcsdinternalapiv2.auth.repository.RefreshTokenRepository;
import com.bcsdlab.bcsdinternalapiv2.auth.security.JwtTokenProvider;
import com.bcsdlab.bcsdinternalapiv2.auth.service.AuthService;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.util.GithubIdNormalizer;
import com.bcsdlab.bcsdinternalapiv2.member.util.PhoneNumberNormalizer;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.InitialSetupRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.MemberContactUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PasswordChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.MemberResponse;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public MemberResponse getMe(Long memberId) {
        return MemberResponse.from(getMemberOrThrow(memberId));
    }

    @Transactional(readOnly = true)
    public MemberResponse getInitialSetupInfo(Long memberId, long tokenPasswordVersion) {
        Member member = getMemberOrThrow(memberId);
        if (!member.isPendingSetup()) {
            throw new MemberException(MemberExceptionType.ALREADY_ACTIVATED);
        }
        if (JwtTokenProvider.passwordVersion(member) != tokenPasswordVersion) {
            throw new AuthException(AuthExceptionType.UNAUTHORIZED);
        }
        return MemberResponse.from(member);
    }

    @Transactional
    public LoginResponse completeInitialSetup(Long memberId, long tokenPasswordVersion, InitialSetupRequest request,
                                               HttpServletRequest servletRequest,
                                               HttpServletResponse servletResponse) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.UNAUTHORIZED));
        if (!member.isPendingSetup()) {
            throw new MemberException(MemberExceptionType.ALREADY_ACTIVATED);
        }
        if (JwtTokenProvider.passwordVersion(member) != tokenPasswordVersion) {
            throw new AuthException(AuthExceptionType.UNAUTHORIZED);
        }

        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new AuthException(AuthExceptionType.PASSWORD_CONFIRM_MISMATCH);
        }

        String normalizedPhone = PhoneNumberNormalizer.normalize(request.phoneNumber());
        String normalizedGithubId = GithubIdNormalizer.normalize(request.githubId());
        String normalizedEmail = request.email().trim().toLowerCase();

        if (memberRepository.existsByEmailAndIdNot(normalizedEmail, memberId)) {
            throw new MemberException(MemberExceptionType.EMAIL_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(request.newPassword());
        member.completeInitialSetup(encodedPassword, normalizedPhone, normalizedEmail, normalizedGithubId,
                Instant.now());

        return authService.issueFullSessionAfterSetup(member, servletRequest, servletResponse);
    }

    @Transactional
    public void updateContact(Long memberId, MemberContactUpdateRequest request) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.UNAUTHORIZED));

        String normalizedPhone = PhoneNumberNormalizer.normalize(request.phoneNumber());
        String normalizedGithubId = GithubIdNormalizer.normalize(request.githubId());
        String normalizedEmail = request.email().trim().toLowerCase();

        if (memberRepository.existsByEmailAndIdNot(normalizedEmail, memberId)) {
            throw new MemberException(MemberExceptionType.EMAIL_DUPLICATED);
        }

        member.updateContact(normalizedPhone, normalizedEmail, normalizedGithubId);
    }

    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest request) {
        if (!request.newPassword().equals(request.newPasswordConfirm())) {
            throw new AuthException(AuthExceptionType.PASSWORD_CONFIRM_MISMATCH);
        }

        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.currentPassword(), member.getPassword())) {
            throw new AuthException(AuthExceptionType.CURRENT_PASSWORD_MISMATCH);
        }

        Instant now = Instant.now();
        member.changePassword(passwordEncoder.encode(request.newPassword()), now);

        refreshTokenRepository.findAllByMemberIdAndRevokedAtIsNull(memberId)
                .forEach(refreshToken -> refreshToken.revoke(now));
    }

    private Member getMemberOrThrow(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthExceptionType.UNAUTHORIZED));
    }
}
