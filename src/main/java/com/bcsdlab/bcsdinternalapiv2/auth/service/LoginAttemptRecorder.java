package com.bcsdlab.bcsdinternalapiv2.auth.service;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginAttemptRecorder {

    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedLogin(Long memberId, Instant now) {
        memberRepository.findByIdForUpdate(memberId).ifPresent(member -> member.recordFailedLogin(now));
    }
}
