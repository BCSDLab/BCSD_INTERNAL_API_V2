package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.member.client.SlackClient;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackProfileSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.model.MemberStatus;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Slack 조회(네트워크 호출)는 트랜잭션 밖에서 수행하고, DB 반영은
 * {@link MemberDirectoryService#updatePhotoUrl}을 외부에서 호출해 프록시를 경유하도록 한다.
 * 같은 클래스 안에서 @Transactional 메서드를 self-invocation으로 호출하면 프록시가
 * 적용되지 않아 변경 사항이 커밋되지 않는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackProfileSyncService {

    private final MemberRepository memberRepository;
    private final SlackClient slackClient;
    private final MemberDirectoryService memberDirectoryService;

    public SlackProfileSyncResponse syncAll() {
        List<Member> members = memberRepository.findAllByStatus(MemberStatus.ACTIVE);
        int updated = 0;
        int failed = 0;
        for (Member member : members) {
            try {
                Optional<String> imageUrl = slackClient.findProfileImageUrlByEmail(member.getEmail());
                if (imageUrl.isEmpty()) {
                    throw new IllegalStateException("Slack에서 프로필 이미지를 찾을 수 없습니다.");
                }
                memberDirectoryService.updatePhotoUrl(member.getId(), imageUrl.get());
                updated++;
            } catch (RuntimeException e) {
                failed++;
                log.warn("SLACK_PROFILE_SYNC_FAILED: memberId={}", member.getId(), e);
            }
        }
        return new SlackProfileSyncResponse(members.size(), updated, failed);
    }
}
