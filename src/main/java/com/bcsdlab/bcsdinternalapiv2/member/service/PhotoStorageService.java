package com.bcsdlab.bcsdinternalapiv2.member.service;

import com.bcsdlab.bcsdinternalapiv2.global.storage.S3UploadUrlSigner;
import com.bcsdlab.bcsdinternalapiv2.global.storage.S3UploadUrlSigner.PresignedUpload;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.request.PhotoPresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.PhotoPresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 회원 프로필 사진도 미디어 도메인의 이미지 업로드와 동일하게 presigned URL로 처리한다 —
 * 서버는 이미지 바이트를 경유하지 않는다. 브라우저가 발급받은 uploadUrl로 S3에 직접 PUT한
 * 뒤, 그 결과 publicUrl을 별도로 회원 프로필(profile_image_url)에 저장한다.
 */
@Service
@RequiredArgsConstructor
public class PhotoStorageService {

    private final S3UploadUrlSigner s3UploadUrlSigner;

    public PhotoPresignedUrlResponse issuePresignedUrl(Long memberId, PhotoPresignedUrlRequest request) {
        PresignedUpload upload = s3UploadUrlSigner.presignPut(
                "member-photos/" + memberId, request.fileName(), request.contentType(), null);
        return new PhotoPresignedUrlResponse(upload.uploadUrl(), upload.publicUrl());
    }
}
