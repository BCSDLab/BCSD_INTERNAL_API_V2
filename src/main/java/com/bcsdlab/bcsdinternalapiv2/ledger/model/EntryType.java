package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 장부 기록의 방향. 금액은 항상 양수로 저장하고 방향은 이 값으로 나타낸다. */
public enum EntryType {
    DEPOSIT,
    WITHDRAWAL;

    /** 저장된 양수 금액에 방향을 붙인다. 입금은 그대로, 출금은 음수다. */
    public long signed(long amount) {
        return this == DEPOSIT ? amount : -amount;
    }
}
