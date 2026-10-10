package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * 학기 자연키. 1학기는 3~8월, 2학기는 9월~다음 해 2월(6개월)이다. 경로와 응답에서는 "2026-2" 형식을 쓴다.
 */
public record SemesterKey(int year, int term) implements Comparable<SemesterKey> {

    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{4}-[12]$");
    private static final int MONTHS_PER_SEMESTER = 6;

    public static SemesterKey parse(String semesterId) {
        if (semesterId == null || !ID_PATTERN.matcher(semesterId).matches()) {
            throw new LedgerException(LedgerExceptionType.INVALID_SEMESTER_ID);
        }
        return new SemesterKey(Integer.parseInt(semesterId.substring(0, 4)), semesterId.charAt(5) - '0');
    }

    /** 날짜가 속한 학기. 1~2월은 전년도 2학기다. */
    public static SemesterKey of(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 8) {
            return new SemesterKey(date.getYear(), 1);
        }
        if (month >= 9) {
            return new SemesterKey(date.getYear(), 2);
        }
        return new SemesterKey(date.getYear() - 1, 2);
    }

    public SemesterKey next() {
        return term == 1 ? new SemesterKey(year, 2) : new SemesterKey(year + 1, 1);
    }

    /** 학기 순서 비교용 값(year × 2 + term). */
    public int order() {
        return year * 2 + term;
    }

    public List<YearMonth> months() {
        YearMonth start = YearMonth.of(year, term == 1 ? 3 : 9);
        return IntStream.range(0, MONTHS_PER_SEMESTER).mapToObj(start::plusMonths).toList();
    }

    public String id() {
        return year + "-" + term;
    }

    @Override
    public int compareTo(SemesterKey other) {
        return Integer.compare(order(), other.order());
    }

    @Override
    public String toString() {
        return id();
    }
}
