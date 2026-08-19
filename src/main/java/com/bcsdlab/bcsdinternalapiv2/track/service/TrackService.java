package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.CurriculumResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.service.CurriculumQueryService;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.StudyPointResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageTechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackStudyPointRepository;
import java.util.List;
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

        return TrackDetailResponse.of(trackPage, studyPoints, techStacks, curriculum);
    }
}
