package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.SlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import com.bcsdlab.bcsdinternalapiv2.track.util.SlugGenerator;
import java.time.Instant;
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

    public List<AdminTrackPageSummaryResponse> getTrackPages() {
        return trackPageRepository.findAllByOrderByDisplayOrderAsc().stream()
                .map(AdminTrackPageSummaryResponse::from)
                .toList();
    }

    public AdminTrackPageDetailResponse getTrackPage(Long id) {
        return AdminTrackPageDetailResponse.from(findTrackPageOrThrow(id));
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

    private TrackPage findTrackPageOrThrow(Long id) {
        return trackPageRepository.findById(id)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_PAGE_NOT_FOUND));
    }
}
