package com.bcsdlab.bcsdinternalapiv2.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 컨테이너 없는 순수 로직 테스트(ADR-019). */
class SlugGeneratorTest {

    @Test
    @DisplayName("AC-1.1 표시명에서 slug를 파생한다")
    void 표시명에서_slug를_파생한다() {
        assertThat(SlugGenerator.from("Data Analyst")).isEqualTo("data-analyst");
        assertThat(SlugGenerator.from("Frontend")).isEqualTo("frontend");
        assertThat(SlugGenerator.from("  iOS / Swift  ")).isEqualTo("ios-swift");
    }

    @Test
    @DisplayName("영숫자가 없는 표시명은 빈 문자열이 된다 — 그대로 쓰면 slug 유니크 제약과 충돌한다")
    void 한글_표시명은_빈_slug가_된다() {
        assertThat(SlugGenerator.from("백엔드")).isEmpty();
        assertThat(SlugGenerator.from("···")).isEmpty();
        assertThat(SlugGenerator.from(null)).isEmpty();
    }

    @Test
    @DisplayName("fromOrFallback은 처음으로 비지 않는 후보를 쓴다")
    void 비지_않는_첫_후보를_쓴다() {
        assertThat(SlugGenerator.fromOrFallback("백엔드", "BACKEND", "track-7")).isEqualTo("backend");
        assertThat(SlugGenerator.fromOrFallback("Frontend", "FRONTEND", "track-7")).isEqualTo("frontend");
        // 표시명·코드 모두 한글이면 마지막 후보로 떨어진다.
        assertThat(SlugGenerator.fromOrFallback("백엔드", "백엔드", "track-7")).isEqualTo("track-7");
    }
}
