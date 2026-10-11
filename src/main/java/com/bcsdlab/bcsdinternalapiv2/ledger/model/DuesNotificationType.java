package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 납부 안내(PAYMENT)와 미납 안내(UNPAID). 서버는 type별 대상 조건을 강제하지 않고 기록에만 남긴다. */
public enum DuesNotificationType {
    PAYMENT,
    UNPAID
}
