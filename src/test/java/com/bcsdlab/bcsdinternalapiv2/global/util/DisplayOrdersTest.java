package com.bcsdlab.bcsdinternalapiv2.global.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bcsdlab.bcsdinternalapiv2.global.exception.GlobalException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DisplayOrdersTest {

    @Test
    @DisplayName("INV-3 요청 순서대로 0부터 시작하는 연속 정수를 부여한다")
    void 요청_순서대로_0부터_연속으로_부여한다() {
        Map<Long, Integer> result = DisplayOrders.reassign(List.of(3L, 1L, 2L), List.of(1L, 2L, 3L));

        assertThat(result).containsExactly(
                Map.entry(3L, 0),
                Map.entry(1L, 1),
                Map.entry(2L, 2)
        );
    }

    @Test
    @DisplayName("빈 목록끼리는 예외 없이 빈 결과를 반환한다")
    void 빈_목록은_빈_결과를_반환한다() {
        assertThat(DisplayOrders.reassign(List.of(), List.of())).isEmpty();
    }

    @Test
    @DisplayName("AC-1.5 요청에 존재하지 않는 대상이 누락되면 400 성격의 예외를 던진다")
    void 대상이_누락되면_예외를_던진다() {
        assertThatThrownBy(() -> DisplayOrders.reassign(List.of(1L, 2L), List.of(1L, 2L, 3L)))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("AC-1.5 요청에 존재하지 않는 id가 추가되면 예외를 던진다")
    void 존재하지_않는_id가_추가되면_예외를_던진다() {
        assertThatThrownBy(() -> DisplayOrders.reassign(List.of(1L, 2L, 3L, 4L), List.of(1L, 2L, 3L)))
                .isInstanceOf(GlobalException.class);
    }

    @Test
    @DisplayName("AC-1.5 요청 안에 id가 중복되면 예외를 던진다")
    void 요청_안에_id가_중복되면_예외를_던진다() {
        // INV-4: reassign은 순수 함수라 예외 시 Map을 전혀 만들지 않는다 — "일부만 적용된 상태"가
        // 존재할 수 없다. 호출자는 반환값을 받은 뒤에만 엔티티를 변경한다.
        assertThatThrownBy(() -> DisplayOrders.reassign(List.of(1L, 1L, 2L), List.of(1L, 2L)))
                .isInstanceOf(GlobalException.class);
    }
}
