package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

/** 지금 상태(before)와 이 면제를 적용했을 때(after)의 그 회원 회비. */
public record ExemptionPreviewResponse(
        MemberDuesResponse before,
        MemberDuesResponse after
) {
}
