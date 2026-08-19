package com.bcsdlab.bcsdinternalapiv2.activity.controller;

import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityImagesReplaceRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.request.ActivityUpdateRequest;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivityDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.controller.dto.response.AdminActivitySummaryResponse;
import com.bcsdlab.bcsdinternalapiv2.activity.service.AdminActivityService;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.OrderRequest;
import com.bcsdlab.bcsdinternalapiv2.global.controller.dto.request.PublishRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/activities")
@RequiredArgsConstructor
public class AdminActivityController implements AdminActivityApi {

    private final AdminActivityService adminActivityService;

    @Override
    @GetMapping
    public Page<AdminActivitySummaryResponse> getActivities(@RequestParam(required = false) Long categoryId,
                                                              @RequestParam(required = false) Integer year,
                                                              @RequestParam(required = false) Boolean published,
                                                              Pageable pageable) {
        return adminActivityService.getActivities(categoryId, year, published, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public AdminActivityDetailResponse getActivity(@PathVariable Long id) {
        return adminActivityService.getActivity(id);
    }

    @Override
    @PostMapping
    public ResponseEntity<AdminActivityDetailResponse> createActivity(
            @Valid @RequestBody ActivityCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminActivityService.createActivity(request));
    }

    @Override
    @PutMapping("/{id}")
    public AdminActivityDetailResponse updateActivity(@PathVariable Long id,
                                                       @Valid @RequestBody ActivityUpdateRequest request) {
        return adminActivityService.updateActivity(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        adminActivityService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Void> publish(@PathVariable Long id, @Valid @RequestBody PublishRequest request) {
        adminActivityService.publish(id, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/order")
    public ResponseEntity<Void> reorder(@RequestParam Long categoryId, @RequestParam int year,
                                         @RequestParam int month, @Valid @RequestBody OrderRequest request) {
        adminActivityService.reorder(categoryId, year, month, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping("/{id}/images")
    public List<String> replaceImages(@PathVariable Long id,
                                       @Valid @RequestBody ActivityImagesReplaceRequest request) {
        return adminActivityService.replaceImages(id, request);
    }
}
