package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 학기 회비 상태. 차이(연결 입금 − 연결 출금 − 부과액) 하나로 정한다. */
public enum SemesterDuesStatus {
    PAID,
    UNPAID,
    EXEMPT,
    OVERPAID
}
