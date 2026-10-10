package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.SemesterKey;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * 회비 집계 규칙을 모은 순수 함수. DB를 모르고, 조회할 때마다 계산한다(저장하지 않음).
 *
 * <p>차이 = 연결 입금 합계 − 연결 출금 합계 − 부과액. 0이면 PAID, 음수면 UNPAID, 양수면 OVERPAID다.
 * 부과액이 0이고 연결 내역이 없으면 EXEMPT다. 연결 내역의 분류(category)는 보지 않는다.
 */
public final class DuesCalculator {

    private static final Comparator<ExemptionPeriod> EXEMPTION_ORDER =
            Comparator.comparing(ExemptionPeriod::startMonth).thenComparingLong(ExemptionPeriod::id);

    private DuesCalculator() {
    }

    /**
     * 회원 한 명의 학기 회비.
     *
     * @param exemptions 그 회원의 면제(겹쳐도 된다). 학기 밖의 면제가 섞여 있어도 된다.
     * @param links      그 (학기, 회원)에 연결된 장부 기록의 방향과 금액
     */
    public static MemberDues calculate(SemesterKey semester, long monthlyAmount, boolean applicable,
                                       List<ExemptionPeriod> exemptions, List<LinkedAmount> links) {
        List<YearMonth> months = semester.months();
        if (!applicable) {
            // 납부 비대상. 비대상 회원에게는 연결이 없다(연결 쪽에서 막는다).
            List<MonthDues> notApplicable = months.stream()
                    .map(month -> new MonthDues(month, MonthDuesStatus.NOT_APPLICABLE, List.of(), null))
                    .toList();
            return new MemberDues(SemesterDuesStatus.EXEMPT, notApplicable, null, null, null, 0);
        }

        List<List<ExemptionPeriod>> exemptionsByMonth = months.stream()
                .map(month -> exemptions.stream().filter(e -> e.covers(month)).sorted(EXEMPTION_ORDER).toList())
                .toList();
        long chargeableMonths = exemptionsByMonth.stream().filter(List::isEmpty).count();
        long assessed = chargeableMonths * monthlyAmount;

        long net = links.stream()
                .mapToLong(link -> link.type() == EntryType.DEPOSIT ? link.amount() : -link.amount())
                .sum();
        long diff = net - assessed;

        List<MonthDues> monthDues = new ArrayList<>(months.size());
        long remaining = Math.max(0, net);
        for (int i = 0; i < months.size(); i++) {
            YearMonth month = months.get(i);
            List<ExemptionPeriod> monthExemptions = exemptionsByMonth.get(i);
            if (!monthExemptions.isEmpty()) {
                monthDues.add(new MonthDues(month, MonthDuesStatus.EXEMPT, monthExemptions, null));
            } else if (remaining >= monthlyAmount) {
                remaining -= monthlyAmount;
                monthDues.add(new MonthDues(month, MonthDuesStatus.PAID, List.of(), null));
            } else if (remaining > 0) {
                // 덜 채운 첫 달에만 남은 금액을 적는다.
                monthDues.add(new MonthDues(month, MonthDuesStatus.UNPAID, List.of(), partialNote(remaining)));
                remaining = 0;
            } else {
                monthDues.add(new MonthDues(month, MonthDuesStatus.UNPAID, List.of(), null));
            }
        }

        SemesterDuesStatus status;
        if (assessed == 0 && links.isEmpty()) {
            status = SemesterDuesStatus.EXEMPT;
        } else if (diff == 0) {
            status = SemesterDuesStatus.PAID;
        } else if (diff < 0) {
            status = SemesterDuesStatus.UNPAID;
        } else {
            status = SemesterDuesStatus.OVERPAID;
        }

        return new MemberDues(status, monthDues, assessed, net, Math.max(0, -diff), Math.max(0, diff));
    }

    /** 학기 요약. FE mock의 deriveSummary와 같은 식이다. */
    public static SemesterSummary summarize(List<MemberDues> members) {
        int total = members.size();
        int exempt = count(members, SemesterDuesStatus.EXEMPT);
        int completed = count(members, SemesterDuesStatus.PAID) + count(members, SemesterDuesStatus.OVERPAID);
        int unpaid = count(members, SemesterDuesStatus.UNPAID);
        long totalAmount = 0;
        long paidAmount = 0;
        long unpaidAmount = 0;
        for (MemberDues member : members) {
            long assessed = orZero(member.assessedAmount());
            totalAmount += assessed;
            paidAmount += Math.max(0, Math.min(orZero(member.paidAmount()), assessed));
            unpaidAmount += orZero(member.unpaidAmount());
        }
        boolean needsReview = members.stream().anyMatch(member ->
                member.status() == SemesterDuesStatus.UNPAID || member.status() == SemesterDuesStatus.OVERPAID);
        return new SemesterSummary(total, exempt, total - exempt, completed, unpaid,
                totalAmount, paidAmount, unpaidAmount, needsReview);
    }

    private static String partialNote(long amount) {
        return String.format(Locale.KOREA, "%,d원 부분 납부", amount);
    }

    private static int count(List<MemberDues> members, SemesterDuesStatus status) {
        return (int) members.stream().filter(member -> member.status() == status).count();
    }

    private static long orZero(Long value) {
        return value == null ? 0 : value;
    }

    /** 면제 기간. endMonth가 null이면 계속이다. */
    public record ExemptionPeriod(long id, String reason, YearMonth startMonth, YearMonth endMonth) {

        boolean covers(YearMonth month) {
            return !month.isBefore(startMonth) && (endMonth == null || !month.isAfter(endMonth));
        }
    }

    public record LinkedAmount(EntryType type, long amount) {
    }

    public record MonthDues(YearMonth month, MonthDuesStatus status, List<ExemptionPeriod> exemptions, String note) {
    }

    /** 납부 비대상이면 assessedAmount·paidAmount·unpaidAmount가 null이고 excessAmount는 0이다. */
    public record MemberDues(SemesterDuesStatus status, List<MonthDues> months, Long assessedAmount,
                             Long paidAmount, Long unpaidAmount, long excessAmount) {
    }

    public record SemesterSummary(int totalMembers, int exemptMembers, int targetMembers, int completedMembers,
                                  int unpaidMembers, long totalAmount, long paidAmount, long unpaidAmount,
                                  boolean needsReview) {
    }
}
