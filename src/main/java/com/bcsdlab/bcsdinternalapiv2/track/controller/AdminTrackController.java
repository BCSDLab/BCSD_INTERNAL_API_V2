package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackMasterUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackMasterResponse;
import com.bcsdlab.bcsdinternalapiv2.track.service.AdminTrackMasterService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/tracks")
@RequiredArgsConstructor
public class AdminTrackController implements AdminTrackApi {

    private final AdminTrackMasterService adminTrackMasterService;

    @Override
    @GetMapping
    public List<TrackMasterResponse> getTracks() {
        return adminTrackMasterService.getTracks();
    }

    @Override
    @PostMapping
    public ResponseEntity<TrackMasterResponse> createTrack(@Valid @RequestBody TrackMasterCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTrackMasterService.createTrack(request));
    }

    @Override
    @PutMapping("/{id}")
    public TrackMasterResponse updateTrack(@PathVariable Long id, @Valid @RequestBody TrackMasterUpdateRequest request) {
        return adminTrackMasterService.updateTrack(id, request);
    }
}
