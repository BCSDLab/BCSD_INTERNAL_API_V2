package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EntryTypeTest {

    @Test
    void 입금은_금액_그대로다() {
        assertThat(EntryType.DEPOSIT.signed(10_000)).isEqualTo(10_000);
    }

    @Test
    void 출금은_음수다() {
        assertThat(EntryType.WITHDRAWAL.signed(10_000)).isEqualTo(-10_000);
    }
}
