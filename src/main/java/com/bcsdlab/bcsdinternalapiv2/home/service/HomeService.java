package com.bcsdlab.bcsdinternalapiv2.home.service;

import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.HomeResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.MentorResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.QnaResponse;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.RecruitLinkResponse;
import com.bcsdlab.bcsdinternalapiv2.home.model.MentorSlot;
import com.bcsdlab.bcsdinternalapiv2.home.model.RecruitLink;
import com.bcsdlab.bcsdinternalapiv2.home.repository.MentorSlotRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.QnaItemRepository;
import com.bcsdlab.bcsdinternalapiv2.home.repository.RecruitLinkRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final MentorSlotRepository mentorSlotRepository;
    private final QnaItemRepository qnaItemRepository;
    private final RecruitLinkRepository recruitLinkRepository;
    private final MemberRepository memberRepository;

    public HomeResponse getHome() {
        List<QnaResponse> qna = qnaItemRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(QnaResponse::from)
                .toList();
        RecruitLinkResponse recruit = recruitLinkRepository.findById(RecruitLink.SINGLETON_ID)
                .map(RecruitLinkResponse::from)
                .orElse(null);

        return new HomeResponse(activeMentors(), qna, recruit);
    }

    /** 트랙 공개 조회(TrackService)와 같은 이유로 N+1을 피한다. */
    private List<MentorResponse> activeMentors() {
        List<MentorSlot> slots = mentorSlotRepository.findAllByOrderByDisplayOrderAsc();
        List<Long> memberIds = slots.stream().map(slot -> slot.getMember().getId()).toList();
        Map<Long, Member> membersById = memberIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllById(memberIds).stream()
                        .collect(Collectors.toMap(Member::getId, member -> member));

        return slots.stream()
                .map(slot -> membersById.get(slot.getMember().getId()))
                .filter(member -> member != null && member.isActive())
                .map(MentorResponse::from)
                .toList();
    }
}
