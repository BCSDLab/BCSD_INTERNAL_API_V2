package com.bcsdlab.bcsdinternalapiv2.global.storage;

import com.bcsdlab.bcsdinternalapiv2.global.config.S3Properties;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * 브라우저가 S3에 직접 PUT할 presigned URL을 만든다(서버는 이미지 바이트를 거치지 않는다).
 * 서명은 네트워크 호출 없이 로컬에서 계산한다.
 */
@Component
@RequiredArgsConstructor
public class S3UploadUrlSigner {

    private static final Duration UPLOAD_URL_VALIDITY = Duration.ofMinutes(10);

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * {@code {keyPrefix}/{UUID}.{확장자}} 키로 PUT URL을 서명한다. contentLength가 null이 아니면 크기도 서명해
     * S3가 다른 크기의 업로드를 거부한다.
     */
    public PresignedUpload presignPut(String keyPrefix, String fileName, String contentType, Long contentLength) {
        String key = "%s/%s.%s".formatted(keyPrefix, UUID.randomUUID(), extensionOf(fileName));
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucket())
                .key(key)
                .contentType(contentType)
                .contentLength(contentLength)
                .build();
        String uploadUrl = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(UPLOAD_URL_VALIDITY)
                        .putObjectRequest(putObjectRequest)
                        .build())
                .url().toString();
        return new PresignedUpload(key, uploadUrl, s3Properties.publicBaseUrl() + "/" + key);
    }

    private static String extensionOf(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    public record PresignedUpload(String key, String uploadUrl, String publicUrl) {
    }
}
