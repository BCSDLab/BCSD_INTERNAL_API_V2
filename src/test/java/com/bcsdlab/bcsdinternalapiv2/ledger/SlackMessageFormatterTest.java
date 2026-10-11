package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.ledger.service.SlackMessageFormatter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

/** 본문 이스케이프 표(사용자 결정 D1). http(s) 링크만 살리고 나머지 & < >는 글자로, {담당자 멘션}은 마지막에 바꾼다. */
class SlackMessageFormatterTest {

    private static final String ADMIN = "U0ADMIN001";

    @ParameterizedTest(name = "[{index}] {0}")
    @CsvSource(delimiter = '¦', textBlock = """
            <https://example.com/rules|회칙>                ¦ <https://example.com/rules|회칙>
            <http://example.com>                           ¦ <http://example.com>
            <https://a.com/x?a=1&b=2|목록 & 문서>            ¦ <https://a.com/x?a=1&amp;b=2|목록 &amp; 문서>
            <!channel> 확인                                 ¦ &lt;!channel&gt; 확인
            <!here>                                        ¦ &lt;!here&gt;
            <!everyone>                                    ¦ &lt;!everyone&gt;
            <!subteam^S123|team>                           ¦ &lt;!subteam^S123|team&gt;
            <@U12345678> 님                                 ¦ &lt;@U12345678&gt; 님
            <#C12345678>                                   ¦ &lt;#C12345678&gt;
            <javascript:alert(1)|클릭>                      ¦ &lt;javascript:alert(1)|클릭&gt;
            <mailto:a@b.com|메일>                            ¦ &lt;mailto:a@b.com|메일&gt;
            A & B < C > D                                  ¦ A &amp; B &lt; C &gt; D
            &amp; 이미 엔티티                                 ¦ &amp;amp; 이미 엔티티
            @channel 평문                                   ¦ @channel 평문
            <https://a.com|라벨 <b>>                         ¦ &lt;https://a.com|라벨 &lt;b&gt;&gt;
            """)
    void 이스케이프_표(String input, String expected) {
        assertThat(SlackMessageFormatter.format(input.strip(), ADMIN)).isEqualTo(expected.strip());
    }

    @Test
    void 담당자_멘션은_이스케이프_뒤에_서버_멘션으로_바꾼다() {
        String body = "문의는 {담당자 멘션} 또는 <https://docs.example.com|문서>로 & 해 주세요. <!channel>";

        assertThat(SlackMessageFormatter.format(body, ADMIN)).isEqualTo(
                "문의는 <@U0ADMIN001> 또는 <https://docs.example.com|문서>로 &amp; 해 주세요. &lt;!channel&gt;");
    }

    @Test
    void 링크_라벨_안의_담당자_멘션은_바꾸지_않는다() {
        assertThat(SlackMessageFormatter.format("<https://a.com|{담당자 멘션}>", ADMIN))
                .isEqualTo("<https://a.com|{담당자 멘션}>");
    }

    @Test
    void 관리자_Slack_ID가_없으면_멘션을_바꾸지_않는다() {
        assertThat(SlackMessageFormatter.format("연락: {담당자 멘션}", null)).isEqualTo("연락: {담당자 멘션}");
        assertThat(SlackMessageFormatter.containsManagerMention("연락: {담당자 멘션}")).isTrue();
        assertThat(SlackMessageFormatter.containsManagerMention("연락처 없음")).isFalse();
    }

    @Test
    void 여러_줄과_여러_링크를_순서대로_처리한다() {
        String body = "[안내]\n<https://a.com|A> 그리고 <https://b.com|B>\n끝 <tag>";

        assertThat(SlackMessageFormatter.format(body, ADMIN))
                .isEqualTo("[안내]\n<https://a.com|A> 그리고 <https://b.com|B>\n끝 &lt;tag&gt;");
    }
}
