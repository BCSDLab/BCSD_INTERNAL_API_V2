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
    public PresignedUrlResponse issuePresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return adminImageService.issuePresignedUrl(request);
    }

    @Override
    @PostMapping("/{id}/complete")
    public ImageCompleteResponse complete(@PathVariable Long id) {
        return adminImageService.complete(id);
    }

    @Override
    @GetMapping
    public Page<ImageAssetResponse> getImages(@RequestParam ImagePurpose purpose, Pageable pageable) {
        return adminImageService.getImages(purpose, pageable);
    }
}
