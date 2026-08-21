package com.bcsdlab.bcsdinternalapiv2.activity.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ActivityContentSanitizerTest {

    @Test
    @DisplayName("AC-3.5 script 태그는 저장 시 제거된다")
    void script_태그는_제거된다() {
        String result = ActivityContentSanitizer.sanitize("<p>안내</p><script>alert(1)</script>");

        assertThat(result).doesNotContainIgnoringCase("<script");
    }

    @Test
    @DisplayName("이벤트 핸들러 속성은 제거된다")
    void 이벤트_핸들러_속성은_제거된다() {
        String result = ActivityContentSanitizer.sanitize("<p onclick=\"alert(1)\">안내</p>");

        assertThat(result).doesNotContain("onclick");
    }

    @Test
    @DisplayName("허용 목록 밖 태그(iframe)는 제거된다")
    void 허용_목록_밖_태그는_제거된다() {
        String result = ActivityContentSanitizer.sanitize("<p>안내</p><iframe src=\"https://evil.com\"></iframe>");

        assertThat(result).doesNotContainIgnoringCase("<iframe");
    }

    @Test
    @DisplayName("AC-3.6 허용되지 않은 호스트의 이미지는 제거된다")
    void 허용되지_않은_호스트의_이미지는_제거된다() {
        String result = ActivityContentSanitizer.sanitize("<img src=\"http://evil.com/x.png\">");

        assertThat(result).doesNotContain("evil.com");
    }

    @Test
    @DisplayName("허용된 호스트의 이미지는 보존된다")
    void 허용된_호스트의_이미지는_보존된다() {
        String result = ActivityContentSanitizer.sanitize("<img src=\"https://image.bcsdlab.com/a.png\" alt=\"a\">");

        assertThat(result).contains("image.bcsdlab.com/a.png");
    }

    @Test
    @DisplayName("허용 태그·속성은 그대로 보존된다 (과잉 정제 방지)")
    void 허용_태그와_속성은_보존된다() {
        String result = ActivityContentSanitizer.sanitize(
                "<p>제 1회 <strong>BCSD Lab Conference</strong>를 개최했습니다.</p>"
                        + "<ul><li>세션 A</li><li>세션 B</li></ul>"
                        + "<a href=\"https://bcsdlab.com\" target=\"_blank\" rel=\"noopener\">링크</a>");

        assertThat(result)
                .contains("<strong>BCSD Lab Conference</strong>")
                .contains("<li>세션 A</li>")
                .contains("href=\"https://bcsdlab.com\"");
    }

    @Test
    @DisplayName("null 입력은 null을 반환한다")
    void null_입력은_null을_반환한다() {
        assertThat(ActivityContentSanitizer.sanitize(null)).isNull();
    }
}
