package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 장부 분류. 회비에 연결한 기록은 입금·출금 모두 DUES다(반환 분류 없음). */
public enum LedgerCategory {
    DUES,
    EVENT,
    OPERATION,
    ETC
}
