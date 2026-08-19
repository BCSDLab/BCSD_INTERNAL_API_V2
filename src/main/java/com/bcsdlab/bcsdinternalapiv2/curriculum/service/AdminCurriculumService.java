package com.bcsdlab.bcsdinternalapiv2.curriculum.service;

import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.CurriculumCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.request.CurriculumUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.curriculum.controller.dto.response.AdminCurriculumSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.curriculum.exception.CurriculumException;
import com.bcsdlab.bcsdinternalapiv2.curriculum.exception.CurriculumExceptionType;
import com.bcsdlab.bcsdinternalapiv2.curriculum.model.Curriculum;
import com.bcsdlab.bcsdinternalapiv2.curriculum.repository.CurriculumRepository;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final TrackPageRepository trackPageRepository;
    private final AdminCurriculumTreeService adminCurriculumTreeService;

    public List<AdminCurriculumSummaryResponse> getCurriculums(Long trackPageId) {
        return curriculumRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPageId).stream()
                .map(AdminCurriculumSummaryResponse::from)
                .toList();
    }

    @Transactional
    public AdminCurriculumSummaryResponse createCurriculum(Long trackPageId, CurriculumCreateRequest request) {
        TrackPage trackPage = trackPageRepository.findById(trackPageId)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_NOT_FOUND));

        String name = request.name();
        Curriculum source = null;
        if (request.isClone()) {
            source = findOrThrow(request.sourceCurriculumId());
            if (name == null || name.isBlank()) {
                name = source.getName();
            }
        } else if (name == null || name.isBlank()) {
            throw new CurriculumException(CurriculumExceptionType.NAME_REQUIRED);
        }

        int displayOrder = curriculumRepository.findAllByTrackPage_IdOrderByDisplayOrderAsc(trackPageId).size();
        Curriculum curriculum = curriculumRepository.save(Curriculum.builder()
                .trackPage(trackPage)
                .name(name)
                .published(false)
                .displayOrder(displayOrder)
                .build());

        if (source != null) {
            adminCurriculumTreeService.cloneTree(source, curriculum);
        }
        return AdminCurriculumSummaryResponse.from(curriculum);
    }

    @Transactional
    public AdminCurriculumSummaryResponse updateCurriculum(Long id, CurriculumUpdateRequest request) {
        Curriculum curriculum = findOrThrow(id);
        curriculum.rename(request.name());
        return AdminCurriculumSummaryResponse.from(curriculum);
    }

    @Transactional
    public void deleteCurriculum(Long id) {
        findOrThrow(id).delete(Instant.now());
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        Curriculum curriculum = findOrThrow(id);
        if (request.isPublished()) {
            // AC-2.1: 벌크 UPDATE로 다른 공개 세트를 먼저 내려야, 뒤이은 dirty-check 반영 시
            // uq_curriculum_published(partial unique index)에 걸리지 않는다.
            curriculumRepository.unpublishOthers(curriculum.getTrackPage().getId(), id);
        }
        curriculum.updatePublished(request.isPublished());
    }

    private Curriculum findOrThrow(Long id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new CurriculumException(CurriculumExceptionType.CURRICULUM_NOT_FOUND));
    }
}
