package com.bcsdlab.bcsdinternalapiv2.ledger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SemesterKeyTest {

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({
            "2027-01-01, 2026-2",
            "2027-02-28, 2026-2",
            "2028-02-29, 2027-2",
            "2027-03-01, 2027-1",
            "2027-08-31, 2027-1",
            "2027-09-01, 2027-2",
            "2027-12-31, 2027-2",
    })
    void 날짜가_속한_학기(LocalDate date, String expected) {
        assertThat(SemesterKey.of(date).id()).isEqualTo(expected);
    }

    @Test
    void 다음_학기() {
        assertThat(new SemesterKey(2026, 1).next()).isEqualTo(new SemesterKey(2026, 2));
        assertThat(new SemesterKey(2026, 2).next()).isEqualTo(new SemesterKey(2027, 1));
    }

    @Test
    void 일학기의_월은_3월부터_8월까지다() {
        assertThat(new SemesterKey(2027, 1).months())
                .containsExactly(YearMonth.of(2027, 3), YearMonth.of(2027, 4), YearMonth.of(2027, 5),
                        YearMonth.of(2027, 6), YearMonth.of(2027, 7), YearMonth.of(2027, 8));
    }

    @Test
    void 학기_ID를_읽는다() {
        assertThat(SemesterKey.parse("2026-2")).isEqualTo(new SemesterKey(2026, 2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-3", "2026-0", "26-2", "2026_2", "2026-2 ", "abc", ""})
    void 형식이_틀리면_INVALID_SEMESTER_ID(String semesterId) {
        assertThatThrownBy(() -> SemesterKey.parse(semesterId))
                .isInstanceOf(LedgerException.class)
                .extracting(e -> ((LedgerException) e).getExceptionType())
                .isEqualTo(LedgerExceptionType.INVALID_SEMESTER_ID);
    }
}
