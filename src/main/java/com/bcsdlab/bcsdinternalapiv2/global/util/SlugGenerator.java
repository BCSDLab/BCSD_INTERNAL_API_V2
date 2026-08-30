package com.bcsdlab.bcsdinternalapiv2.global.util;

/**
 * 트랙 표시명에서 URL slug를 파생한다(AC-1.1).
 * 소문자로 바꾸고, 영숫자가 아닌 문자는 하이픈으로 뭉친 뒤 양끝 하이픈을 지운다.
 *
 * <p>표시명이 전부 비영숫자면(예: "백엔드") 결과가 빈 문자열이 된다. slug는
 * {@code NOT NULL}이고 {@code uq_track_page_slug}가 붙어 있어서, 빈 slug를 그대로 쓰면
 * 첫 한글 트랙은 만들어지지만 두 번째 한글 트랙이 중복으로 막히고 공개 API
 * {@code GET /v1/tracks/{slug}}도 깨진다. 그래서 파생 결과가 비면 호출자가 대체값을
 * 넘길 수 있게 {@link #fromOrFallback}을 둔다.
 */
public final class SlugGenerator {

    private SlugGenerator() {
    }

    /** 파생만 한다. 영숫자가 하나도 없으면 빈 문자열을 돌려준다. */
    public static String from(String source) {
        return source == null
                ? ""
                : source.toLowerCase()
                        .replaceAll("[^a-z0-9]+", "-")
                        .replaceAll("^-+|-+$", "");
    }

    /**
     * 앞에서부터 순서대로 파생을 시도해 처음으로 비지 않는 slug를 돌려준다.
     * 전부 비면 빈 문자열이므로, 호출자는 마지막 후보를 반드시 영숫자로 준다.
     */
    public static String fromOrFallback(String... candidates) {
        for (String candidate : candidates) {
            String slug = from(candidate);
            if (!slug.isEmpty()) {
                return slug;
            }
        }
        return "";
    }
}
