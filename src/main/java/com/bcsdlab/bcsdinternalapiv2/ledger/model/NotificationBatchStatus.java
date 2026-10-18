package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 회비 알림 발송 건의 상태. DB 체크 제약(ck_dues_notification_batch_status)과 값이 같아야 한다. */
public enum NotificationBatchStatus {
    IN_PROGRESS,
    DONE
}
