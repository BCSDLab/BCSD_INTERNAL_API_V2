package com.bcsdlab.bcsdinternalapiv2.global;

import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.global.util.DisplayOrders;
import java.util.List;
import java.util.function.ToIntFunction;
import org.junit.jupiter.api.Test;

class DisplayOrdersTest {

    private static final ToIntFunction<Integer> SELF = Integer::intValue;

    @Test
    void 형제가_없으면_0이다() {
        assertThat(DisplayOrders.next(List.of(), SELF)).isZero();
    }

    @Test
    void 삭제로_빈_번호가_있어도_마지막_순서_다음이다() {
        assertThat(DisplayOrders.next(List.of(0, 2), SELF)).isEqualTo(3);
    }
}
