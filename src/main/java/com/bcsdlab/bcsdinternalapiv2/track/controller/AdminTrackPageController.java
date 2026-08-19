package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.SlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.StudyPointsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.StudyPointResponse;
import com.bcsdlab.bcsdinternalapiv2.track.service.AdminTrackPageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/track-pages")
@RequiredArgsConstructor
public class AdminTrackPageController implements AdminTrackPageApi {

    private final AdminTrackPageService adminTrackPageService;

    @Override
    @GetMapping
    public List<AdminTrackPageSummaryResponse> getTrackPages() {
        return adminTrackPageService.getTrackPages();
    }

    @Override
    @GetMapping("/{id}")
    public AdminTrackPageDetailResponse getTrackPage(@PathVariable Long id) {
        return adminTrackPageService.getTrackPage(id);
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminTrackPageDetailResponse> createTrackPage(
            @Valid @RequestBody TrackPageCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminTrackPageService.createTrackPage(request));
    }

    @Override
    @PutMapping("/{id}")
    public AdminTrackPageDetailResponse updateTrackPage(@PathVariable Long id,
                                                         @Valid @RequestBody TrackPageUpdateRequest request) {
        return adminTrackPageService.updateTrackPage(id, request);
    }

    @Override
    @PatchMapping("/{id}/slug")
    public AdminTrackPageDetailResponse changeSlug(@PathVariable Long id,
                                                    @Valid @RequestBody SlugChangeRequest request) {
        return adminTrackPageService.changeSlug(id, request);
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request) {
        adminTrackPageService.publish(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request) {
        adminTrackPageService.reorder(request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrackPage(@PathVariable Long id) {
        adminTrackPageService.deleteTrackPage(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/study-points")
    public List<StudyPointResponse> replaceStudyPoints(@PathVariable Long id,
                                                        @Valid @RequestBody StudyPointsReplaceRequest request) {
        return adminTrackPageService.replaceStudyPoints(id, request);
    }
}
