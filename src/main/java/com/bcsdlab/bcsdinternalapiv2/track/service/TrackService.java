package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackPage;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackService {

    private final TrackPageRepository trackPageRepository;

    public List<TrackSummaryResponse> getTracks() {
        return trackPageRepository.findAllByPublishedTrueOrderByDisplayOrderAsc().stream()
                .map(TrackSummaryResponse::from)
                .toList();
    }

    public TrackDetailResponse getTrack(String slug) {
        TrackPage trackPage = trackPageRepository.findBySlugAndPublishedTrue(slug)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_NOT_FOUND));
        return TrackDetailResponse.from(trackPage);
    }
}
