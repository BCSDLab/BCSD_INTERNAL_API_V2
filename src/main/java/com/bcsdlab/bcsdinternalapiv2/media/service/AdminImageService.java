package com.bcsdlab.bcsdinternalapiv2.media.service;

import com.bcsdlab.bcsdinternalapiv2.global.config.S3Properties;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.request.PresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageAssetResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.ImageCompleteResponse;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.response.PresignedUrlResponse;
import com.bcsdlab.bcsdinternalapiv2.media.exception.MediaException;
import com.bcsdlab.bcsdinternalapiv2.media.exception.MediaExceptionType;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * presigned URL 발급 → 브라우저가 S3에 직접 PUT → complete 등록(ADR-009). 서버는 이미지
 * 바이트를 경유하지 않는다 — API 컨테이너 메모리 한도(256MiB) 때문에 서버 측 이미지
 * 변환·버퍼링을 도입하지 않는다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminImageService {

    private static final Duration UPLOAD_URL_VALIDITY = Duration.ofMinutes(10);

    private final ImageAssetRepository imageAssetRepository;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    @Transactional
    public PresignedUrlResponse issuePresignedUrl(PresignedUrlRequest request) {
        String s3Key = "%s/%s.%s".formatted(
                request.purpose().name().toLowerCase(), UUID.randomUUID(), extensionOf(request.fileName()));
        String publicUrl = s3Properties.publicBaseUrl() + "/" + s3Key;

        ImageAsset image = imageAssetRepository.save(ImageAsset.builder()
                .s3Key(s3Key)
                .url(publicUrl)
                .originalName(request.fileName())
                .contentType(request.contentType())
                .byteSize(request.byteSize())
                .purpose(request.purpose())
                .build());

        String uploadUrl = presignPutUrl(s3Key, request.contentType());
        return new PresignedUrlResponse(image.getId(), uploadUrl, publicUrl);
    }

    @Transactional
    public ImageCompleteResponse complete(Long id) {
        ImageAsset image = findOrThrow(id);
        image.confirm();
        return new ImageCompleteResponse(image.getUrl());
    }

    public Page<ImageAssetResponse> getImages(ImagePurpose purpose, Pageable pageable) {
        return imageAssetRepository.findAllByPurposeAndConfirmedTrueOrderByCreatedAtDesc(purpose, pageable)
                .map(ImageAssetResponse::from);
    }

    private String presignPutUrl(String s3Key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(s3Key)
                .contentType(contentType)
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_VALIDITY)
                .putObjectRequest(putObjectRequest)
                .build());
        return presigned.url().toString();
    }

    private String extensionOf(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private ImageAsset findOrThrow(Long id) {
        return imageAssetRepository.findById(id)
                .orElseThrow(() -> new MediaException(MediaExceptionType.IMAGE_NOT_FOUND));
    }
}
