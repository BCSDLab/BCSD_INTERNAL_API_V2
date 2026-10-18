package com.bcsdlab.bcsdinternalapiv2.track.controller;

import com.bcsdlab.bcsdinternalapiv2.auth.security.LoginMemberId;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.SlugChangeRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.StudyPointsReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TechStacksReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.request.TrackPageUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.AdminTrackPageSummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.StudyPointResponse;
import com.bcsdlab.bcsdinternalapiv2.track.controller.dto.response.TechStackResponse;
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
    public ResponseEntity<List<AdminTrackPageSummaryResponse>> getTrackPages() {
        return ResponseEntity.ok(adminTrackPageService.getTrackPages());
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<AdminTrackPageDetailResponse> getTrackPage(@PathVariable Long id) {
        return ResponseEntity.ok(adminTrackPageService.getTrackPage(id));
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminTrackPageDetailResponse> createTrackPage(
            @Valid @RequestBody TrackPageCreateRequest request, @LoginMemberId Long loginMemberId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(adminTrackPageService.createTrackPage(request, loginMemberId));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<AdminTrackPageDetailResponse> updateTrackPage(@PathVariable Long id,
                                                                         @Valid @RequestBody TrackPageUpdateRequest request,
                                                                         @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminTrackPageService.updateTrackPage(id, request, loginMemberId));
    }

    @Override
    @PatchMapping("/{id}/slug")
    public ResponseEntity<AdminTrackPageDetailResponse> changeSlug(@PathVariable Long id,
                                                                    @Valid @RequestBody SlugChangeRequest request,
                                                                    @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminTrackPageService.changeSlug(id, request, loginMemberId));
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request,
                                         @LoginMemberId Long loginMemberId) {
        adminTrackPageService.publish(id, request, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@Valid @RequestBody OrderRequest request,
                                         @LoginMemberId Long loginMemberId) {
        adminTrackPageService.reorder(request, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrackPage(@PathVariable Long id, @LoginMemberId Long loginMemberId) {
        adminTrackPageService.deleteTrackPage(id, loginMemberId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/study-points")
    public ResponseEntity<List<StudyPointResponse>> replaceStudyPoints(@PathVariable Long id,
                                                                        @Valid @RequestBody StudyPointsReplaceRequest request,
                                                                        @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminTrackPageService.replaceStudyPoints(id, request, loginMemberId));
    }

    @Override
    @PutMapping("/{id}/tech-stacks")
    public ResponseEntity<List<TechStackResponse>> replaceTechStacks(@PathVariable Long id,
                                                                      @Valid @RequestBody TechStacksReplaceRequest request,
                                                                      @LoginMemberId Long loginMemberId) {
        return ResponseEntity.ok(adminTrackPageService.replaceTechStacks(id, request, loginMemberId));
    }
}
