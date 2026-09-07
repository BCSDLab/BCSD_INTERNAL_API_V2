package com.bcsdlab.bcsdinternalapiv2.track.service;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackMasterResponse;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackException;
import com.bcsdlab.bcsdinternalapiv2.track.exception.TrackExceptionType;
import com.bcsdlab.bcsdinternalapiv2.track.model.TrackMaster;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackMasterRepository;
import com.bcsdlab.bcsdinternalapiv2.track.repository.TrackPageRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTrackMasterService {

    private final TrackMasterRepository trackMasterRepository;
    private final TrackPageRepository trackPageRepository;

    public List<TrackMasterResponse> getTracks() {
        return trackMasterRepository.findAll().stream()
                .map(track -> TrackMasterResponse.of(track, trackPageRepository.existsByTrack_Id(track.getId())))
                .toList();
    }

    @Transactional
    public TrackMasterResponse createTrack(TrackMasterCreateRequest request) {
        if (trackMasterRepository.existsByCode(request.code())) {
            throw new TrackException(TrackExceptionType.TRACK_CODE_DUPLICATED);
        }

        TrackMaster track = trackMasterRepository.save(
                TrackMaster.builder().code(request.code()).name(request.name()).build());
        return TrackMasterResponse.of(track, false);
    }

    @Transactional
    public TrackMasterResponse updateTrack(Long trackId, TrackMasterUpdateRequest request) {
        TrackMaster track = trackMasterRepository.findById(trackId)
                .orElseThrow(() -> new TrackException(TrackExceptionType.TRACK_NOT_FOUND));

        track.update(request.name(), request.isActive());
        return TrackMasterResponse.of(track, trackPageRepository.existsByTrack_Id(track.getId()));
    }
}
