package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.global.config.LoginPageProperties;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberException;
import com.bcsdlab.bcsdinternalapiv2.member.exception.MemberExceptionType;
import com.bcsdlab.bcsdinternalapiv2.member.util.GithubIdNormalizer;
import com.bcsdlab.bcsdinternalapiv2.member.util.PhoneNumberNormalizer;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.AdminMemberCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.AdminMemberCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.global.mail.MailSender;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminMemberService {

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginPageProperties loginPageProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final MailSender mailSender;

    @Transactional
    public AdminMemberCreateResponse createMember(AdminMemberCreateRequest request) {
        if (memberRepository.existsByStudentNumber(request.studentNumber())) {
            throw new MemberException(MemberExceptionType.STUDENT_NUMBER_DUPLICATED);
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (memberRepository.existsByEmail(normalizedEmail)) {
            throw new MemberException(MemberExceptionType.EMAIL_DUPLICATED);
        }

        String normalizedPhone = isBlank(request.phoneNumber()) ? null
                : PhoneNumberNormalizer.normalize(request.phoneNumber());
        String normalizedGithubId = GithubIdNormalizer.normalize(request.githubId());

        String temporaryPassword = generateTemporaryPassword();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        Member member = Member.builder()
                .studentNumber(request.studentNumber())
                .password(passwordEncoder.encode(temporaryPassword))
                .name(request.name())
                .track(request.track())
                .generation(request.generation())
                .memberType(request.memberType())
                .university(request.university())
                .email(normalizedEmail)
                .phoneNumber(normalizedPhone)
                .githubId(normalizedGithubId)
                .passwordChangedAt(now)
                .build();

        memberRepository.save(member);

        eventPublisher.publishEvent(new AccountCreatedEvent(
                member.getId(), normalizedEmail, member.getStudentNumber(), temporaryPassword,
                loginPageProperties.url(), now));

        return new AdminMemberCreateResponse(member.getId(), member.getStudentNumber());
    }

    @Transactional
    public void resendWelcomeMail(Long memberId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new MemberException(MemberExceptionType.MEMBER_NOT_FOUND));
        if (!member.isPendingSetup()) {
            throw new MemberException(MemberExceptionType.ALREADY_ACTIVATED);
        }

        String temporaryPassword = generateTemporaryPassword();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        member.reissueTemporaryPassword(passwordEncoder.encode(temporaryPassword), now);

        eventPublisher.publishEvent(new AccountCreatedEvent(
                member.getId(), member.getEmail(), member.getStudentNumber(), temporaryPassword,
                loginPageProperties.url(), now));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendWelcomeMailAfterCommit(AccountCreatedEvent event) {
        Instant expectedPasswordChangedAt = event.passwordChangedAt();
        Instant now = Instant.now();

        int claimed = memberRepository.markWelcomeMailSentIfCurrent(event.memberId(), now, expectedPasswordChangedAt);
        if (claimed == 0) {
            log.info("SKIPPING_STALE_WELCOME_MAIL: to={} memberId={} — superseded by a later resend.",
                    event.email(), event.memberId());
            return;
        }

        try {
            mailSender.sendAccountCreated(event.email(), event.studentNumber(), event.temporaryPassword(),
                    event.loginUrl());
        } catch (RuntimeException e) {
            log.error("MAIL_DELIVERY_FAILED: welcome mail to={} studentNumber={} memberId={} — the one-shot "
                    + "temporary password cannot be recovered from here; reverting welcome_mail_sent_at to null so "
                    + "this member is identifiable for an admin-triggered password reset.",
                    event.email(), event.studentNumber(), event.memberId(), e);
            memberRepository.markWelcomeMailSentIfCurrent(event.memberId(), null, expectedPasswordChangedAt);
        }
    }

    private String generateTemporaryPassword() {
        StringBuilder builder = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            builder.append(TEMP_PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return builder.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
