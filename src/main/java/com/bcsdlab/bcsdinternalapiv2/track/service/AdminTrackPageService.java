package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.SlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.StudyPointRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.StudyPointsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TechStacksReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageMemberResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.StudyPointResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TechStack;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPageTechStack;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackStudyPoint;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageMemberRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageTechStackRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackStudyPointRepository;
import com.bcsdlab.bcsdinternalapiv2.track.util.SlugGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTrackPageService {

    private final TrackPageRepository trackPageRepository;
    private final TrackMasterRepository trackMasterRepository;
    private final TrackStudyPointRepository trackStudyPointRepository;
    private final TechStackRepository techStackRepository;
    private final TrackPageTechStackRepository trackPageTechStackRepository;
    private final TrackPageMemberRepository trackPageMemberRepository;

    public List<AdminTrackPageSummaryResponse> getTrackPages() {
        return trackPageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminTrackPageSummaryResponse::from)
                .toList();
    }

    public AdminTrackPageDetailResponse getTrackPage(Long id) {
        TrackPage trackPage = findTrackPageOrThrow(id);

        List<StudyPointResponse> studyPoints = trackStudyPointRepository
                .findAllByTrackPage_IdOrderByDisplayOrderAsc(id).stream()
                .map(StudyPointResponse::from)
                .toList();
        List<TechStackResponse> techStacks = trackPageTechStackRepository
                .findAllByTrackPageIdOrderByDisplayOrderAsc(id).stream()
                .map(tpts -> TechStackResponse.from(tpts.getTechStack()))
                .toList();
        List<AdminTrackPageMemberResponse> members = trackPageMemberRepository
                .findAllByTrackPage_IdOrderByDisplayOrderAsc(id).stream()
                .map(AdminTrackPageMemberResponse::from)
                .toList();

        return AdminTrackPageDetailResponse.of(trackPage, studyPoints, techStacks, members);
    }

    @Transactional
    public AdminTrackPageDetailResponse createTrackPage(TrackPageCreateRequest request) {
        TrackMaster track = trackMasterRepository.findById(request.trackId())
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_NOT_FOUND));
        if (trackPageRepository.existsByTrack_Id(track.getId())) {
            throw new TrackException(TrackExceptionType.TRACK_PAGE_ALREADY_EXISTS);
        }

        String slug = SlugGenerator.from(request.displayName());
        if (trackPageRepository.existsBySlug(slug)) {
            throw new TrackException(TrackExceptionType.TRACK_PAGE_SLUG_DUPLICATED);
        }

        TrackPage trackPage = TrackPage.builder()
                .track(track)
                .slug(slug)
                .displayName(request.displayName())
                .tagline(request.tagline())
                .heroImageUrl(request.heroImageUrl())
                .ogImageUrl(request.ogImageUrl())
                .seoDescription(request.seoDescription())
                .displayOrder((int) trackPageRepository.count())
                .published(true)
                .build();

        return AdminTrackPageDetailResponse.from(trackPageRepository.save(trackPage));
    }

    @Transactional
    public AdminTrackPageDetailResponse updateTrackPage(Long id, TrackPageUpdateRequest request) {
        TrackPage trackPage = findTrackPageOrThrow(id);
        trackPage.updateHeader(request.displayName(), request.tagline(), request.heroImageUrl(),
                request.ogImageUrl(), request.seoDescription());
        return AdminTrackPageDetailResponse.from(trackPage);
    }

    @Transactional
    public AdminTrackPageDetailResponse changeSlug(Long id, SlugChangeRequest request) {
        TrackPage trackPage = findTrackPageOrThrow(id);
        if (!trackPage.getSlug().equals(request.slug()) && trackPageRepository.existsBySlug(request.slug())) {
            throw new TrackException(TrackExceptionType.TRACK_PAGE_SLUG_DUPLICATED);
        }

        trackPage.changeSlug(request.slug());
        return AdminTrackPageDetailResponse.from(trackPage);
    }

    @Transactional
    public void publish(Long id, PublishRequest request) {
        findTrackPageOrThrow(id).updatePublished(request.isPublished());
    }

    @Transactional
    public void reorder(OrderRequest request) {
        List<TrackPage> trackPages = trackPageRepository.findAll();
        Map<Long, TrackPage> byId = trackPages.stream()
                .collect(Collectors.toMap(TrackPage::getId, trackPage -> trackPage));

        Map<Long, Integer> newOrders = DisplayOrders.reassign(request.ids(), byId.keySet());
        newOrders.forEach((trackPageId, order) -> byId.get(trackPageId).updateDisplayOrder(order));
    }

    @Transactional
    public void deleteTrackPage(Long id) {
        findTrackPageOrThrow(id).delete(Instant.now());
    }

    @Transactional
    public List<StudyPointResponse> replaceStudyPoints(Long id, StudyPointsReplaceRequest request) {
        TrackPage trackPage = findTrackPageOrThrow(id);
        trackStudyPointRepository.deleteAllByTrackPage_Id(id);

        List<StudyPointRequest> items = request.studyPoints();
        List<TrackStudyPoint> saved = new ArrayList<>(items.size());
        for (int i = 0; i < items.size(); i++) {
            StudyPointRequest item = items.get(i);
            saved.add(trackStudyPointRepository.save(TrackStudyPoint.builder()
                    .trackPage(trackPage)
                    .title(item.title())
                    .description(item.description())
                    .iconImageUrl(item.iconImageUrl())
                    .displayOrder(i)
                    .build()));
        }
        return saved.stream().map(StudyPointResponse::from).toList();
    }

    @Transactional
    public List<TechStackResponse> replaceTechStacks(Long id, TechStacksReplaceRequest request) {
        TrackPage trackPage = findTrackPageOrThrow(id);
        List<Long> techStackIds = request.techStackIds();

        Map<Long, TechStack> byId = techStackRepository.findAllById(techStackIds).stream()
                .collect(Collectors.toMap(TechStack::getId, techStack -> techStack));
        if (byId.size() != new HashSet<>(techStackIds).size()) {
            throw new TrackException(TrackExceptionType.TECH_STACK_NOT_FOUND);
        }

        trackPageTechStackRepository.deleteAllByTrackPage_Id(id);

        List<TrackPageTechStack> saved = new ArrayList<>(techStackIds.size());
        for (int i = 0; i < techStackIds.size(); i++) {
            saved.add(trackPageTechStackRepository.save(
                    new TrackPageTechStack(trackPage, byId.get(techStackIds.get(i)), i)));
        }
        return saved.stream().map(tpts -> TechStackResponse.from(tpts.getTechStack())).toList();
    }

    private TrackPage findTrackPageOrThrow(Long id) {
        return trackPageRepository.findById(id)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_NOT_FOUND));
    }
}
