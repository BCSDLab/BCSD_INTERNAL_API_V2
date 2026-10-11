package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 증빙 링크 한 건. url·label은 앞뒤 공백을 지운 뒤 검사하고 그대로 저장한다(그 밖의 정규화는 하지 않는다).
 * label은 지운 뒤 비어 있으면 null이다. 구글 드라이브 링크인지는 {@link #isGoogleDriveUrl()}로 서비스가 본다.
 */
public record EvidenceLinkRequest(
        @Schema(example = "https://drive.google.com/file/d/abc/view")
        @NotBlank(message = "증빙 링크를 입력하세요.")
        @Size(max = 2048, message = "증빙 링크는 2,048자 이하여야 합니다.")
        String url,

        @Schema(example = "영수증", nullable = true)
        @Size(max = 50, message = "링크 이름은 50자 이하로 입력하세요.")
        String label
) {

    private static final Set<String> ALLOWED_HOSTS = Set.of("drive.google.com", "docs.google.com");

    public EvidenceLinkRequest {
        url = url == null ? null : url.strip();
        label = label == null || label.isBlank() ? null : label.strip();
    }

    /**
     * https이고 호스트가 구글 드라이브·문서인지(호스트·스킴은 대소문자 무시). 이미 그 기록에 붙어 있던 링크(V27이 옮긴
     * 예전 S3 증빙)는 다시 보내도 통과시켜야 해서, 요청 검증이 아니라 서비스가 새 링크에만 이 검사를 한다.
     */
    public boolean isGoogleDriveUrl() {
        if (url == null) {
            return false;
        }
        try {
            URI uri = new URI(url);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getRawUserInfo() == null
                    && uri.getPort() == -1
                    && uri.getHost() != null
                    && ALLOWED_HOSTS.contains(uri.getHost().toLowerCase(Locale.ROOT));
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /** 같은 기록 안에 같은 url이 두 번 있는지. */
    public static boolean hasDuplicateUrl(List<EvidenceLinkRequest> links) {
        return links.stream().map(EvidenceLinkRequest::url).distinct().count() != links.size();
    }
}
