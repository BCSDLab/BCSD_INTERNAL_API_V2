package com.bcsdlab.bcsdinternalapiv2.home.service;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedPublisher;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.request.MentorSlotCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.home.controller.dto.response.AdminMentorSlotResponse;
import com.bcsdlab.bcsdinternalapiv2.home.exception.HomeException;
import com.bcsdlab.bcsdinternalapiv2.home.exception.HomeExceptionType;
import com.bcsdlab.bcsdinternalapiv2.home.model.MentorSlot;
import com.bcsdlab.bcsdinternalapiv2.home.repository.MentorSlotRepository;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메인 멘토 캐러셀 노출/순서를 관리한다(ADR-012 패턴 재사용). 명부는 이미 구현되어
 * 있으므로 포트 추상화 없이 {@code MemberRepository}를 직접 조회한다. 이름·사진·
 * 트랙에 대한 쓰기 경로는 만들지 않는다(INV-19).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMentorSlotService {

    private final MentorSlotRepository mentorSlotRepository;
    private final MemberRepository memberRepository;
    private final ContentChangedPublisher contentChangedPublisher;

    public List<AdminMentorSlotResponse> getSlots() {
        return mentorSlotRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminMentorSlotResponse::from)
                .toList();
    }

    @Transactional
    public List<AdminMentorSlotResponse> addSlot(MentorSlotCreateRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new HomeException(HomeExceptionType.MEMBER_NOT_FOUND));
        if (mentorSlotRepository.existsByMember_Id(member.getId())) {
            throw new HomeException(HomeExceptionType.MENTOR_ALREADY_ASSIGNED);
        }

        int nextOrder = (int) mentorSlotRepository.count();
        mentorSlotRepository.save(MentorSlot.builder().member(member).displayOrder(nextOrder).build());
        contentChangedPublisher.homeChanged();
        return getSlots();
    }

    @Transactional
    public void removeSlot(Long memberId) {
        MentorSlot slot = mentorSlotRepository.findByMember_Id(memberId)
                .orElseThrow(() -> new HomeException(HomeExceptionType.MENTOR_SLOT_NOT_FOUND));
        mentorSlotRepository.delete(slot);
        contentChangedPublisher.homeChanged();
    }

    @Transactional
    public void reorder(OrderRequest request) {
        List<MentorSlot> slots = mentorSlotRepository.findAll();
        Map<Long, MentorSlot> byId = slots.stream().collect(Collectors.toMap(MentorSlot::getId, slot -> slot));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
        contentChangedPublisher.homeChanged();
    }
}
