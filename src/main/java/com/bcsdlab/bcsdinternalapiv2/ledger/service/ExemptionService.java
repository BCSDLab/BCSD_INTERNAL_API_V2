package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.request.ExemptionDraft;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionReasonListResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response.ExemptionResponse;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerException;
import com.bcsdlab.bcsdinternalapiv2.ledger.exception.LedgerExceptionType;
import com.bcsdlab.bcsdinternalapiv2.ledger.model.DuesExemption;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesExemptionReasonRepository;
import com.bcsdlab.bcsdinternalapiv2.ledger.repository.DuesExemptionRepository;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회비 면제 저장과 사유 관리. 겹침 검사는 하지 않으므로 회원 행 잠금도 필요 없다. 면제는 학기에 묶이지 않아,
 * 저장하면 겹치는 모든 학기의 집계가 다음 조회부터 바뀐다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExemptionService {

    private final DuesExemptionRepository exemptionRepository;
    private final DuesExemptionReasonRepository reasonRepository;
    private final MemberRepository memberRepository;

    public ExemptionListResponse getExemptions() {
        return new ExemptionListResponse(
                exemptionRepository.findAllByOrderByIdAsc().stream().map(ExemptionResponse::from).toList());
    }

    public ExemptionReasonListResponse getReasons() {
        return new ExemptionReasonListResponse(reasonRepository.findAllNames());
    }

    @Transactional
    public ExemptionResponse create(ExemptionDraft draft, Long adminId) {
        validate(draft);
        DuesExemption exemption = exemptionRepository.save(new DuesExemption(
                draft.memberId(), draft.reason(), draft.note(), draft.startMonth(), draft.endMonth(), adminId));
        registerReason(draft.reason());
        return ExemptionResponse.from(exemption);
    }

    @Transactional
    public ExemptionResponse update(Long exemptionId, ExemptionDraft draft) {
        DuesExemption exemption = exemptionRepository.findById(exemptionId)
                .orElseThrow(() -> new LedgerException(LedgerExceptionType.EXEMPTION_NOT_FOUND));
        validate(draft);
        exemption.update(draft.memberId(), draft.reason(), draft.note(), draft.startMonth(), draft.endMonth());
        registerReason(draft.reason());
        return ExemptionResponse.from(exemption);
    }

    private void validate(ExemptionDraft draft) {
        if (!draft.hasValidPeriod()) {
            throw new LedgerException(LedgerExceptionType.INVALID_EXEMPTION_PERIOD);
        }
        if (!memberRepository.existsById(draft.memberId())) {
            throw new LedgerException(LedgerExceptionType.MEMBER_NOT_FOUND);
        }
    }

    /**
     * 처음 쓰는 사유를 목록에 등록한다(요청 DTO가 이미 NFC·공백 정규화를 했다). 미리 확인하고, 그 사이 다른 요청이
     * 같은 사유를 넣었으면 ON CONFLICT DO NOTHING이 건너뛴다 — 제약 위반 예외(전역 400)가 나지 않는다.
     */
    private void registerReason(String reason) {
        if (!reasonRepository.exists(reason)) {
            reasonRepository.insertIfAbsent(reason);
        }
    }
}
