package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.global.config.S3Properties;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoPresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PhotoPresignedUrlResponse;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 회원 프로필 사진도 미디어 도메인의 이미지 업로드와 동일하게 presigned URL로 처리한다 —
 * 서버는 이미지 바이트를 경유하지 않는다. 브라우저가 발급받은 uploadUrl로 S3에 직접 PUT한
 * 뒤, 그 결과 publicUrl을 별도로 회원 프로필(profile_image_url)에 저장한다.
 */
@Service
@RequiredArgsConstructor
public class PhotoStorageService {

    private static final Duration UPLOAD_URL_VALIDITY = Duration.ofMinutes(10);

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public PhotoPresignedUrlResponse issuePresignedUrl(Long memberId, PhotoPresignedUrlRequest request) {
        String key = "member-photos/%d/%s.%s".formatted(memberId, UUID.randomUUID(), extensionOf(request.fileName()));
        String publicUrl = s3Properties.publicBaseUrl() + "/" + key;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(request.contentType())
                .build();
        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(UPLOAD_URL_VALIDITY)
                .putObjectRequest(putObjectRequest)
                .build());

        return new PhotoPresignedUrlResponse(presigned.url().toString(), publicUrl);
    }

    private String extensionOf(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }
}
