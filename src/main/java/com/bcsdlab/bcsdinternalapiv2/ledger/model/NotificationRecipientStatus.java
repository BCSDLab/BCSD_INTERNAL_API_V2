package com.bcsdlab.bcsdinternalapiv2.ledger.model;

/** 회원별 알림 발송 상태. DB 체크 제약(ck_dues_notification_recipient_status)과 값이 같아야 한다. */
public enum NotificationRecipientStatus {
    PENDING,
    SENT,
    FAILED
}
