package com.bcsdlab.bcsdinternalapiv2.activity.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

/**
 * 활동 본문(리치텍스트)을 저장 시점에 정제한다(ADR-008). 렌더 시점에는 추가 정제를 하지
 * 않는다 — 저장이 유일한 관문이다.
 *
 * <p>허용 태그는 ADR-008이 정한 목록 그대로다. {@code img[src]}는 jsoup의 Safelist만으로는
 * 호스트 단위 제한이 안 되므로, safelist로 태그/속성을 거른 뒤 별도로 이미지 호스트를
 * 검사해서 허용 목록 밖이면 태그째 제거한다(INV-10).
 */
public final class ActivityContentSanitizer {

    private static final Set<String> ALLOWED_IMAGE_HOSTS = Set.of("image.bcsdlab.com", "static.koreatech.in");

    private static final Safelist SAFELIST = new Safelist()
            .addTags("p", "br", "strong", "em", "u", "s", "ul", "ol", "li", "blockquote",
                    "h2", "h3", "code", "pre", "a", "img")
            .addAttributes("a", "href", "target", "rel")
            .addAttributes("img", "src", "alt", "width")
            .addProtocols("a", "href", "http", "https")
            .addProtocols("img", "src", "http", "https");

    private ActivityContentSanitizer() {
    }

    public static String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return null;
        }

        String cleaned = Jsoup.clean(rawHtml, SAFELIST);
        Document document = Jsoup.parseBodyFragment(cleaned);
        for (Element img : document.select("img")) {
            if (!isAllowedImageHost(img.attr("src"))) {
                img.remove();
            }
        }
        return document.body().html();
    }

    private static boolean isAllowedImageHost(String src) {
        try {
            String host = new URI(src).getHost();
            return host != null && ALLOWED_IMAGE_HOSTS.contains(host);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
