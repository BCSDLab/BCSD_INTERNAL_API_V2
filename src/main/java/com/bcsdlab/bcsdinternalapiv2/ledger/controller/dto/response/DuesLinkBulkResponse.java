package com.bcsdlab.bcsdinternalapiv2.ledger.controller.dto.response;

/** 실제로 연결한 수. 없는 내역과 이미 연결된 내역은 건너뛰고 세지 않는다. */
public record DuesLinkBulkResponse(
        int linkedCount
) {
}
