package com.bcsdlab.bcsdinternalapiv2.track.util;

/**
 * 트랙 표시명(영문, 예: "Data Analyst")에서 URL slug를 파생한다(AC-1.1).
 * 소문자로 바꾸고, 영숫자가 아닌 문자는 하이픈으로 뭉친 뒤 양끝 하이픈을 지운다.
 */
public final class SlugGenerator {

    private SlugGenerator() {
    }

    public static String from(String displayName) {
        return displayName.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }
}
