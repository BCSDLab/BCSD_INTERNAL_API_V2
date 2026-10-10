package com.bcsdlab.bcsdinternalapiv2.ledger;

import static com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus.EXEMPT;
import static com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus.NOT_APPLICABLE;
import static com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus.PAID;
import static com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus.UNPAID;
import static org.assertj.core.api.Assertions.assertThat;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.ExemptionPeriod;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.LinkedAmount;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MemberDues;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MonthDues;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.SemesterSummary;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** 설계 3-2절 예시표(월 10,000원, 2026-2학기 = 2026-09 ~ 2027-02)를 그대로 옮긴다. */
class DuesCalculatorTest {

    private static final SemesterKey SEMESTER = new SemesterKey(2026, 2);
    private static final long MONTHLY = 10_000;

    @Nested
    @DisplayName("3-2절 예시표")
    class Examples {

        @Test
        void 육만원_입금이면_PAID이고_6개월_PAID() {
            MemberDues dues = calculate(List.of(), deposit(60_000));

            assertAmounts(dues, SemesterDuesStatus.PAID, 60_000L, 60_000L, 0L, 0);
            assertMonths(dues, PAID, PAID, PAID, PAID, PAID, PAID);
        }

        @Test
        void 칠만원_입금이면_OVERPAID이고_초과_만원() {
            MemberDues dues = calculate(List.of(), deposit(70_000));

            assertAmounts(dues, SemesterDuesStatus.OVERPAID, 60_000L, 70_000L, 0L, 10_000);
            assertMonths(dues, PAID, PAID, PAID, PAID, PAID, PAID);
        }

        @Test
        void 칠만원_입금과_만원_출금이면_PAID() {
            MemberDues dues = calculate(List.of(), deposit(70_000), withdrawal(10_000));

            assertAmounts(dues, SemesterDuesStatus.PAID, 60_000L, 60_000L, 0L, 0);
            assertMonths(dues, PAID, PAID, PAID, PAID, PAID, PAID);
        }

        @Test
        void 칠만원_입금과_만오천원_출금이면_UNPAID이고_6번째_달은_부분_납부() {
            MemberDues dues = calculate(List.of(), deposit(70_000), withdrawal(15_000));

            assertAmounts(dues, SemesterDuesStatus.UNPAID, 60_000L, 55_000L, 5_000L, 0);
            assertMonths(dues, PAID, PAID, PAID, PAID, PAID, UNPAID);
            assertThat(dues.months().get(5).note()).isEqualTo("5,000원 부분 납부");
            assertThat(dues.months().subList(0, 5)).extracting(MonthDues::note).containsOnlyNulls();
        }

        @Test
        void 십이월부터_면제이고_육만원_입금이면_OVERPAID() {
            MemberDues dues = calculate(List.of(exemption(1, "휴학", "2026-12", null)), deposit(60_000));

            assertAmounts(dues, SemesterDuesStatus.OVERPAID, 30_000L, 60_000L, 0L, 30_000);
            assertMonths(dues, PAID, PAID, PAID, EXEMPT, EXEMPT, EXEMPT);
        }

        @Test
        void 전_기간_면제이고_연결이_없으면_EXEMPT() {
            MemberDues dues = calculate(List.of(exemption(1, "멘토", "2026-01", null)));

            assertAmounts(dues, SemesterDuesStatus.EXEMPT, 0L, 0L, 0L, 0);
            assertMonths(dues, EXEMPT, EXEMPT, EXEMPT, EXEMPT, EXEMPT, EXEMPT);
        }

        @Test
        void 전_기간_면제에_만원_오입금이면_OVERPAID() {
            MemberDues dues = calculate(List.of(exemption(1, "멘토", "2026-01", null)), deposit(10_000));

            assertAmounts(dues, SemesterDuesStatus.OVERPAID, 0L, 10_000L, 0L, 10_000);
            assertMonths(dues, EXEMPT, EXEMPT, EXEMPT, EXEMPT, EXEMPT, EXEMPT);
        }

        @Test
        void 십일월에_면제가_겹치면_그_달에_둘_다_담고_부과액은_한_달만_빠진다() {
            ExemptionPeriod mentor = exemption(7, "멘토", "2026-11", "2026-11");
            ExemptionPeriod leave = exemption(3, "휴학", "2026-11", "2026-11");

            MemberDues dues = calculate(List.of(mentor, leave), deposit(60_000));

            assertAmounts(dues, SemesterDuesStatus.OVERPAID, 50_000L, 60_000L, 0L, 10_000);
            assertMonths(dues, PAID, PAID, EXEMPT, PAID, PAID, PAID);
            // (start_month, id) 오름차순
            assertThat(dues.months().get(2).exemptions()).containsExactly(leave, mentor);
        }

        @Test
        void 납부_비대상이면_금액은_null이고_전_월_NOT_APPLICABLE() {
            MemberDues dues = DuesCalculator.calculate(SEMESTER, MONTHLY, false, List.of(), List.of());

            assertAmounts(dues, SemesterDuesStatus.EXEMPT, null, null, null, 0);
            assertMonths(dues, NOT_APPLICABLE, NOT_APPLICABLE, NOT_APPLICABLE, NOT_APPLICABLE,
                    NOT_APPLICABLE, NOT_APPLICABLE);
            assertThat(dues.months()).allSatisfy(month -> {
                assertThat(month.exemptions()).isEmpty();
                assertThat(month.note()).isNull();
            });
        }
    }

    @Nested
    @DisplayName("상태와 월 표시")
    class StatusAndMonths {

        @Test
        void 연결이_없으면_UNPAID이고_전_월_UNPAID에_메모가_없다() {
            MemberDues dues = calculate(List.of());

            assertAmounts(dues, SemesterDuesStatus.UNPAID, 60_000L, 0L, 60_000L, 0);
            assertMonths(dues, UNPAID, UNPAID, UNPAID, UNPAID, UNPAID, UNPAID);
            assertThat(dues.months()).extracting(MonthDues::note).containsOnlyNulls();
        }

        @Test
        void 부과액이_0이고_연결_입출금이_상쇄되면_PAID() {
            MemberDues dues = calculate(List.of(exemption(1, "멘토", "2026-01", null)),
                    deposit(10_000), withdrawal(10_000));

            assertAmounts(dues, SemesterDuesStatus.PAID, 0L, 0L, 0L, 0);
        }

        @Test
        void 순납부액이_음수면_미납액이_부과액보다_크고_월은_모두_UNPAID() {
            MemberDues dues = calculate(List.of(), withdrawal(5_000));

            assertAmounts(dues, SemesterDuesStatus.UNPAID, 60_000L, -5_000L, 65_000L, 0);
            assertMonths(dues, UNPAID, UNPAID, UNPAID, UNPAID, UNPAID, UNPAID);
            assertThat(dues.months()).extracting(MonthDues::note).containsOnlyNulls();
        }

        @Test
        void 면제_달은_건너뛰고_다음_달부터_채운다() {
            MemberDues dues = calculate(List.of(exemption(1, "휴학", "2026-09", "2026-10")), deposit(25_000));

            assertMonths(dues, EXEMPT, EXEMPT, PAID, PAID, UNPAID, UNPAID);
            assertThat(dues.months().get(4).note()).isEqualTo("5,000원 부분 납부");
            assertThat(dues.months().get(5).note()).isNull();
        }

        @Test
        void 면제가_아닌_달의_exemptions는_빈_배열이다() {
            MemberDues dues = calculate(List.of(exemption(1, "휴학", "2026-12", "2026-12")), deposit(60_000));

            assertThat(dues.months().get(0).exemptions()).isEmpty();
            assertThat(dues.months().get(3).exemptions()).hasSize(1);
        }

        @Test
        void 학기_밖의_면제는_영향이_없다() {
            MemberDues dues = calculate(List.of(exemption(1, "휴학", "2026-03", "2026-08")), deposit(60_000));

            assertAmounts(dues, SemesterDuesStatus.PAID, 60_000L, 60_000L, 0L, 0);
        }

        @Test
        void 이학기의_월은_9월부터_다음_해_2월까지다() {
            assertThat(calculate(List.of()).months()).extracting(MonthDues::month).containsExactly(
                    YearMonth.of(2026, 9), YearMonth.of(2026, 10), YearMonth.of(2026, 11),
                    YearMonth.of(2026, 12), YearMonth.of(2027, 1), YearMonth.of(2027, 2));
        }
    }

    @Nested
    @DisplayName("학기 요약")
    class Summary {

        @Test
        void 요약은_FE_mock_deriveSummary와_같은_식이다() {
            MemberDues paid = calculate(List.of(), deposit(60_000));
            MemberDues overpaid = calculate(List.of(), deposit(70_000));
            MemberDues unpaid = calculate(List.of(), deposit(70_000), withdrawal(15_000));
            MemberDues exempt = calculate(List.of(exemption(1, "멘토", "2026-01", null)));
            MemberDues notApplicable = DuesCalculator.calculate(SEMESTER, MONTHLY, false, List.of(), List.of());

            SemesterSummary summary = DuesCalculator.summarize(
                    List.of(paid, overpaid, unpaid, exempt, notApplicable));

            assertThat(summary.totalMembers()).isEqualTo(5);
            assertThat(summary.exemptMembers()).isEqualTo(2);
            assertThat(summary.targetMembers()).isEqualTo(3);
            assertThat(summary.completedMembers()).isEqualTo(2);
            assertThat(summary.unpaidMembers()).isEqualTo(1);
            assertThat(summary.totalAmount()).isEqualTo(180_000);
            // 초과 납부분은 부과액까지만 센다: 60,000 + 60,000 + 55,000
            assertThat(summary.paidAmount()).isEqualTo(175_000);
            assertThat(summary.unpaidAmount()).isEqualTo(5_000);
            assertThat(summary.needsReview()).isTrue();
        }

        @Test
        void 모두_PAID이거나_EXEMPT면_needsReview는_false() {
            SemesterSummary summary = DuesCalculator.summarize(List.of(
                    calculate(List.of(), deposit(60_000)),
                    DuesCalculator.calculate(SEMESTER, MONTHLY, false, List.of(), List.of())));

            assertThat(summary.needsReview()).isFalse();
        }

        @Test
        void OVERPAID만_있어도_needsReview는_true() {
            SemesterSummary summary = DuesCalculator.summarize(List.of(calculate(List.of(), deposit(70_000))));

            assertThat(summary.needsReview()).isTrue();
            assertThat(summary.unpaidMembers()).isZero();
        }

        @Test
        void 순납부액이_음수여도_요약_납부액은_0_밑으로_내려가지_않는다() {
            SemesterSummary summary = DuesCalculator.summarize(List.of(calculate(List.of(), withdrawal(5_000))));

            assertThat(summary.paidAmount()).isZero();
            assertThat(summary.unpaidAmount()).isEqualTo(65_000);
        }

        @Test
        void 명단이_비면_모두_0이다() {
            SemesterSummary summary = DuesCalculator.summarize(List.of());

            assertThat(summary).isEqualTo(new SemesterSummary(0, 0, 0, 0, 0, 0, 0, 0, false));
        }
    }

    private static MemberDues calculate(List<ExemptionPeriod> exemptions, LinkedAmount... links) {
        return DuesCalculator.calculate(SEMESTER, MONTHLY, true, exemptions, List.of(links));
    }

    private static LinkedAmount deposit(long amount) {
        return new LinkedAmount(EntryType.DEPOSIT, amount);
    }

    private static LinkedAmount withdrawal(long amount) {
        return new LinkedAmount(EntryType.WITHDRAWAL, amount);
    }

    private static ExemptionPeriod exemption(long id, String reason, String start, String end) {
        return new ExemptionPeriod(id, reason, YearMonth.parse(start), end == null ? null : YearMonth.parse(end));
    }

    private static void assertAmounts(MemberDues dues, SemesterDuesStatus status, Long assessed, Long paid,
                                      Long unpaid, long excess) {
        assertThat(dues.status()).isEqualTo(status);
        assertThat(dues.assessedAmount()).isEqualTo(assessed);
        assertThat(dues.paidAmount()).isEqualTo(paid);
        assertThat(dues.unpaidAmount()).isEqualTo(unpaid);
        assertThat(dues.excessAmount()).isEqualTo(excess);
    }

    private static void assertMonths(MemberDues dues, MonthDuesStatus... expected) {
        assertThat(dues.months()).extracting(MonthDues::status).containsExactly(expected);
    }
}
