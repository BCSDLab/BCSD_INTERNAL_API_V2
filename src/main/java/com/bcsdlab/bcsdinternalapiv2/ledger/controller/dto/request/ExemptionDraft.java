package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request;

import java.text.Normalizer;
import java.time.YearMonth;

/** 면제 저장·미리보기 요청의 공통 값(정규화·파싱을 마친 것). */
public record ExemptionDraft(Long memberId, String reason, String note, YearMonth startMonth, YearMonth endMonth) {

    static final String MONTH_PATTERN = "^\\d{4}-(0[1-9]|1[0-2])$";

    /** 앞뒤 공백을 지우고 NFC로 맞춘다. 맥에서 붙여 넣은 한글(NFD)이 다른 사유로 등록되지 않게 한다. */
    static String normalizeReason(String reason) {
        return reason == null ? null : Normalizer.normalize(reason.strip(), Normalizer.Form.NFC);
    }

    static YearMonth parseMonth(String month) {
        return month == null ? null : YearMonth.parse(month);
    }

    /** 종료 월이 없으면 계속이다. */
    public boolean hasValidPeriod() {
        return endMonth == null || !endMonth.isBefore(startMonth);
    }
}
