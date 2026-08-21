package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.MemberVisibilityRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageMembersAttachRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageMember;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 트랙 페이지에 부원을 배정한다(ADR-012). 명부는 이미 구현되어 있으므로 포트 추상화 없이
 * {@code MemberRepository}를 직접 조회한다. 이름·등급·프로필 사진에 대한 쓰기 경로는
 * 만들지 않는다(INV-13) — 이 서비스는 배정·순서·숨김만 다룬다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTrackPageMemberService {

    private final TrackPageRepository trackPageRepository;
    private final TrackPageMemberRepository trackPageMemberRepository;
    private final MemberRepository memberRepository;

    public List<AdminTrackPageMemberResponse> getMembers(Long trackPageId) {
        findTrackPageOrThrow(trackPageId);
        return trackPageMemberRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPageId).stream()
                .map(AdminTrackPageMemberResponse::from)
                .toList();
    }

    @Transactional
    public List<AdminTrackPageMemberResponse> attachMembers(Long trackPageId, TrackPageMembersAttachRequest request) {
        TrackPage trackPage = findTrackPageOrThrow(trackPageId);

        List<Long> memberIds = request.memberIds();
        if (new HashSet<>(memberIds).size() != memberIds.size()) {
            throw new TrackException(TrackExceptionType.MEMBER_ALREADY_ASSIGNED);
        }
        Map<Long, Member> byId = memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
        if (byId.size() != memberIds.size()) {
            throw new TrackException(TrackExceptionType.MEMBER_NOT_FOUND);
        }
        for (Long memberId : memberIds) {
            if (trackPageMemberRepository.existsByTrackPage_IdAndMember_Id(trackPageId, memberId)) {
                throw new TrackException(TrackExceptionType.MEMBER_ALREADY_ASSIGNED);
            }
        }

        int nextOrder = trackPageMemberRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPageId).size();
        for (Long memberId : memberIds) {
            trackPageMemberRepository.save(TrackPageMember.builder()
                    .trackPage(trackPage)
                    .member(byId.get(memberId))
                    .displayOrder(nextOrder++)
                    .visible(true)
                    .build());
        }
        return getMembers(trackPageId);
    }

    @Transactional
    public void detachMember(Long trackPageId, Long memberId) {
        findTrackPageOrThrow(trackPageId);
        TrackPageMember assignment = trackPageMemberRepository
                .findByTrackPage_IdAndMember_Id(trackPageId, memberId)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_MEMBER_NOT_FOUND));
        trackPageMemberRepository.delete(assignment);
    }

    @Transactional
    public void updateVisibility(Long trackPageId, Long memberId, MemberVisibilityRequest request) {
        findTrackPageOrThrow(trackPageId);
        TrackPageMember assignment = trackPageMemberRepository
                .findByTrackPage_IdAndMember_Id(trackPageId, memberId)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_MEMBER_NOT_FOUND));
        assignment.updateVisible(request.isVisible());
    }

    @Transactional
    public void reorder(Long trackPageId, OrderRequest request) {
        findTrackPageOrThrow(trackPageId);
        List<TrackPageMember> assignments = trackPageMemberRepository
                .findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPageId);
        Map<Long, TrackPageMember> byId = assignments.stream()
                .collect(Collectors.toMap(TrackPageMember::getId, assignment -> assignment));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((id, order) -> byId.get(id).updateDisplayOrder(order));
    }

    private TrackPage findTrackPageOrThrow(Long id) {
        return trackPageRepository.findById(id)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_NOT_FOUND));
    }
}
