package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.MonthDuesStatus;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.ExemptionPeriod;
import com.bcsdlab.bcsdinternalapiv2.ledger.service.DuesCalculator.MonthDues;
import java.util.List;

/** month는 "2026-09" 형식. exemptions는 EXEMPT 달에 걸린 면제 전부((startMonth, id) 순), 그 밖은 빈 배열이다. */
public record MonthDuesResponse(
        String month,
        MonthDuesStatus status,
        List<MonthExemptionResponse> exemptions,
        String note
) {

    public static MonthDuesResponse from(MonthDues month) {
        return new MonthDuesResponse(
                month.month().toString(),
                month.status(),
                month.exemptions().stream().map(MonthExemptionResponse::from).toList(),
                month.note());
    }

    public record MonthExemptionResponse(long id, String reason, String startMonth, String endMonth) {

        static MonthExemptionResponse from(ExemptionPeriod exemption) {
            return new MonthExemptionResponse(
                    exemption.id(),
                    exemption.reason(),
                    exemption.startMonth().toString(),
                    exemption.endMonth() == null ? null : exemption.endMonth().toString());
        }
    }
}
