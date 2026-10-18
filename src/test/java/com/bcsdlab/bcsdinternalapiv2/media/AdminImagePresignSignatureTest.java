package com.bcsdlab.bcsdinternalapiv2.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bcsdlab.bcsdinternalapiv2.global.config.S3Properties;
import com.bcsdlab.bcsdinternalapiv2.global.storage.S3UploadUrlSigner;
import com.bcsdlab.bcsdinternalapiv2.media.controller.dto.request.PresignedUrlRequest;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImageAsset;
import com.bcsdlab.bcsdinternalapiv2.media.model.ImagePurpose;
import com.bcsdlab.bcsdinternalapiv2.media.repository.ImageAssetRepository;
import com.bcsdlab.bcsdinternalapiv2.media.service.AdminImageService;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * 실제 S3Presigner(가짜 키, 네트워크 없음)로 서명 헤더를 확인한다. 장부 증빙은 업로드 확인을 끌 수 있으므로,
 * presign에 Content-Type과 Content-Length가 서명돼 S3가 다른 파일을 거부한다는 것이 마지막 방어선이다.
 */
class AdminImagePresignSignatureTest {

    private S3Presigner presigner;
    private AdminImageService service;

    @BeforeEach
    void setUp() {
        presigner = S3Presigner.builder()
                .region(Region.US_WEST_2)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("AKIATEST", "secret")))
                .build();
        ImageAssetRepository repository = mock(ImageAssetRepository.class);
        when(repository.save(any(ImageAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new AdminImageService(repository, new S3UploadUrlSigner(presigner,
                new S3Properties("us-west-2", "test-bucket", "https://image.example.com")));
    }

    @AfterEach
    void tearDown() {
        presigner.close();
    }

    @Test
    void 장부_증빙_URL은_형식과_크기를_서명한다() {
        String url = service.issueUpload(ImagePurpose.LEDGER_EVIDENCE, "a.pdf", "application/pdf", 2048, 1L, true)
                .uploadUrl();

        assertThat(signedHeaders(url)).contains("content-length", "content-type");
        assertThat(url).contains("/ledger_evidence/");
    }

    @Test
    void 기존_미디어_URL은_예전처럼_형식만_서명한다() {
        String url = service.issuePresignedUrl(
                new PresignedUrlRequest("hero.png", "image/png", 1000L, ImagePurpose.TRACK_HERO)).uploadUrl();

        assertThat(signedHeaders(url)).contains("content-type").doesNotContain("content-length");
    }

    private static String signedHeaders(String url) {
        for (String param : url.substring(url.indexOf('?') + 1).split("&")) {
            if (param.startsWith("X-Amz-SignedHeaders=")) {
                return URLDecoder.decode(param.substring("X-Amz-SignedHeaders=".length()), StandardCharsets.UTF_8);
            }
        }
        throw new AssertionError("X-Amz-SignedHeaders가 없습니다: " + url);
    }
}
