package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TrackSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.service.TrackService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tracks")
@RequiredArgsConstructor
public class TrackController implements TrackApi {

    private final TrackService trackService;

    @Override
    @GetMapping
    public ResponseEntity<List<TrackSummaryResponse>> getTracks() {
        return ResponseEntity.ok(trackService.getTracks());
    }

    @Override
    @GetMapping("/{slug}")
    public ResponseEntity<TrackDetailResponse> getTrack(@PathVariable String slug) {
        return ResponseEntity.ok(trackService.getTrack(slug));
    }
}
