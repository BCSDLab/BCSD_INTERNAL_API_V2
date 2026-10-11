package com.bcsdlab.bcsdinternalapiv2.ledger.service;

import com.bcsdlab.bcsdinternalapiv2.ledger.model.EntryType;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;

/**
 * 은행 거래 지문. SHA-256_hex("{yyyy-MM-dd'T'HH:mm:ss}|{type}|{amount}|{bankBalance}|{NFC(trim(counterparty))}").
 * 같은 초·같은 금액이어도 잔액이 다르면 구분된다. 중복 가져오기는 이 값의 부분 유니크 인덱스로 막는다.
 */
public final class RowKeys {

    public static final DateTimeFormatter OCCURRED_AT = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss");

    private RowKeys() {
    }

    public static String of(LocalDateTime occurredAt, EntryType type, long amount, long bankBalance,
                            String counterparty) {
        String source = String.join("|", occurredAt.format(OCCURRED_AT), type.name(), Long.toString(amount),
                Long.toString(bankBalance), ShinhanXlsxParser.normalizeCounterparty(counterparty));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
