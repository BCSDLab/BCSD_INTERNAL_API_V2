package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 장부 기록의 방향. 금액은 항상 양수로 저장하고 방향은 이 값으로 나타낸다. */
public enum EntryType {
    DEPOSIT,
    WITHDRAWAL
}
