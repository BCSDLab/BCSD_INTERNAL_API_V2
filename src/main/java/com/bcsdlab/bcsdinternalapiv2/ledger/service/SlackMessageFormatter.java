package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 관리자가 쓴 본문을 Slack mrkdwn으로 안전하게 만든다(사용자 결정 D1).
 *
 * <ol>
 *   <li>{@code <http(s)://…|라벨>}과 {@code <http(s)://…>} 링크만 링크로 살린다(FE 템플릿이 일부러 쓴다).</li>
 *   <li>나머지 {@code &}, {@code <}, {@code >}는 {@code &amp;}, {@code &lt;}, {@code &gt;}로 바꾼다. 그래서 관리자가 쓴
 *       {@code <!channel>}, {@code <!here>}, {@code <@U…>}, {@code <#C…>}, {@code <!subteam^…>}, {@code <javascript:…>}는
 *       멘션·링크가 되지 않고 글자로 보인다.</li>
 *   <li>마지막에 링크 밖의 {@code {담당자 멘션}}을 서버가 만든 {@code <@관리자 Slack ID>}로 바꾼다. 이 멘션만 실제 멘션이다.</li>
 * </ol>
 */
public final class SlackMessageFormatter {

    public static final String MANAGER_MENTION = "{담당자 멘션}";
    private static final Pattern LINK = Pattern.compile("<(https?://[^\\s<>|]+)(?:\\|([^<>|]*))?>");

    private SlackMessageFormatter() {
    }

    public static boolean containsManagerMention(String body) {
        return body.contains(MANAGER_MENTION);
    }

    /** senderSlackId가 null이면 {담당자 멘션}을 바꾸지 않는다(그 경우 서비스가 미리 거절한다). */
    public static String format(String body, String senderSlackId) {
        StringBuilder out = new StringBuilder(body.length() + 32);
        Matcher matcher = LINK.matcher(body);
        int last = 0;
        while (matcher.find()) {
            out.append(text(body.substring(last, matcher.start()), senderSlackId));
            out.append('<').append(escapeAmpersand(matcher.group(1)));
            if (matcher.group(2) != null) {
                out.append('|').append(escapeAmpersand(matcher.group(2)));
            }
            out.append('>');
            last = matcher.end();
        }
        out.append(text(body.substring(last), senderSlackId));
        return out.toString();
    }

    private static String text(String segment, String senderSlackId) {
        String escaped = segment.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return senderSlackId == null ? escaped : escaped.replace(MANAGER_MENTION, "<@" + senderSlackId + ">");
    }

    private static String escapeAmpersand(String value) {
        return value.replace("&", "&amp;");
    }
}
