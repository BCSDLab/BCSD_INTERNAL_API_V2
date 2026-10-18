package com.bcsdlab.bcsdinternalapiv2.media.controller;

import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.request.PresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageAssetResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageCompleteResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.PresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.service.AdminImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin/images")
@RequiredArgsConstructor
public class AdminImageController implements AdminImageApi {

    private final AdminImageService adminImageService;

    @Override
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> issuePresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return ResponseEntity.ok(adminImageService.issuePresignedUrl(request));
    }

    @Override
    @PostMapping("/{id}/complete")
    public ResponseEntity<ImageCompleteResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(adminImageService.complete(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<Page<ImageAssetResponse>> getImages(@RequestParam ImagePurpose purpose, Pageable pageable) {
        return ResponseEntity.ok(adminImageService.getImages(purpose, pageable));
    }
}
