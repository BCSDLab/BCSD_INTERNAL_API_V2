package com.bcsdlab.bcsdinternalapiv2.home.service;

import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.RecruitLinkUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminRecruitLinkResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.RecruitLinkHistoryResponse;
import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLink;
import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLinkHistory;
import com.bcsdlab.bcsdinternalapiv2.home.repository.RecruitLinkHistoryRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.RecruitLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모집 링크 설정(FR-8.3). {@code recruit_link}는 항상 정확히 1행이라(INV-22) CRUD가
 * 아니라 upsert 하나로 다룬다. 저장할 때마다 {@code recruit_link_history}에 스냅샷을
 * 남긴다 — 유일하게 변경 이력을 남기는 콘텐츠다(01-requirements.md 범위 밖 예외).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminRecruitLinkService {

    private final RecruitLinkRepository recruitLinkRepository;
    private final RecruitLinkHistoryRepository recruitLinkHistoryRepository;
    private final MemberRepository memberRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public AdminRecruitLinkResponse getCurrent() {
        return recruitLinkRepository.findById(RecruitLink.SINGLETON_ID)
                .map(AdminRecruitLinkResponse::from)
                .orElse(null);
    }

    public List<RecruitLinkHistoryResponse> getHistory() {
        return recruitLinkHistoryRepository.findAllByOrderByChangedAtDesc().stream()
                .map(RecruitLinkHistoryResponse::from)
                .toList();
    }

    @Transactional
    public AdminRecruitLinkResponse update(RecruitLinkUpdateRequest request, Long actorMemberId) {
        Instant now = Instant.now();
        RecruitLink recruitLink = recruitLinkRepository.findById(RecruitLink.SINGLETON_ID)
                .orElseGet(() -> RecruitLink.create(
                        request.googleFormUrl(), request.isOpen(), request.closeDate(),
                        request.closedMessage(), now));
        recruitLink.update(request.googleFormUrl(), request.isOpen(), request.closeDate(),
                request.closedMessage(), now);
        RecruitLink saved = recruitLinkRepository.save(recruitLink);

        Member actor = memberRepository.findById(actorMemberId).orElse(null);
        recruitLinkHistoryRepository.save(RecruitLinkHistory.builder()
                .googleFormUrl(request.googleFormUrl())
                .open(request.isOpen())
                .changedBy(actor)
                .changedAt(now)
                .build());

        contentChangedPublisher.homeChanged();
        return AdminRecruitLinkResponse.from(saved);
    }
}
