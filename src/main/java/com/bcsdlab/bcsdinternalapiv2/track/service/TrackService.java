package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.CurriculumQueryService;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.StudyPointResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageMember;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageTechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackStudyPointRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackPageRepository trackPageRepository;
    private final TrackStudyPointRepository trackStudyPointRepository;
    private final TrackPageTechStackRepository trackPageTechStackRepository;
    private final TrackPageMemberRepository trackPageMemberRepository;
    private final MemberRepository memberRepository;
    private final CurriculumQueryService curriculumQueryService;

    public List<TrackSummaryResponse> getTracks() {
        return trackPageRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(TrackSummaryResponse::from)
                .toList();
    }

    public TrackDetailResponse getTrack(String slug) {
        TrackPage trackPage = trackPageRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_NOT_FOUND));

        List<StudyPointResponse> studyPoints = trackStudyPointRepository
                .findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPage.getId()).stream()
                .map(StudyPointResponse::from)
                .toList();

        List<TechStackSummaryResponse> techStacks = trackPageTechStackRepository
                .findAllByTrackPageIdOrderByDisplayOrderAsc(trackPage.getId()).stream()
                .map(tpts -> TechStackSummaryResponse.from(tpts.getTechStack()))
                .toList();

        CurriculumResponse curriculum = curriculumQueryService.getPublishedCurriculum(trackPage.getId())
                .orElse(null);

        List<TrackMemberResponse> members = publishedMembers(trackPage.getId());

        return TrackDetailResponse.of(trackPage, studyPoints, techStacks, curriculum, members);
    }

    /**
     * 홈페이지 빌드가 매번 호출하는 공개 경로라 커리큘럼 조회와 같은 이유로 N+1을 피한다 —
     * 배정 목록 1쿼리 + 부원 IN절 배치 조회 1쿼리로 끝낸다.
     */
    private List<TrackMemberResponse> publishedMembers(Long trackPageId) {
        List<TrackPageMember> assignments = trackPageMemberRepository
                .findAllByTrackPage_IdAndVisibleTrueOrderByDisplayOrderAsc(trackPageId);
        List<Long> memberIds = assignments.stream().map(a -> a.getMember().getId()).toList();
        Map<Long, Member> membersById = memberIds.isEmpty()
                ? Map.of()
                : memberRepository.findAllById(memberIds).stream()
                        .collect(Collectors.toMap(Member::getId, member -> member));

        return assignments.stream()
                .map(a -> membersById.get(a.getMember().getId()))
                .filter(member -> member != null && member.isActive())
                .map(TrackMemberResponse::from)
                .toList();
    }
}
