package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 장부 내역 한 줄. 은행 거래 원본이라 시각·방향·금액·은행 잔액은 바꾸지 않는다.
 * 응답 balance는 계산 값이 아니라 은행이 준 거래 후 잔액(bank_balance)이다.
 */
@Getter
@Entity
@Table(name = "ledger_entry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Column(name = "occurred_seq", nullable = false, updatable = false)
    private short occurredSeq;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private LedgerCategory category;

    @Column(name = "counterparty", nullable = false)
    private String counterparty;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "note", nullable = false)
    private String note;

    @Column(name = "amount", nullable = false, updatable = false)
    private long amount;

    @Column(name = "bank_balance", nullable = false, updatable = false)
    private long bankBalance;

    @Column(name = "source_file_name", updatable = false)
    private String sourceFileName;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "row_key", length = 64, updatable = false)
    private String rowKey;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    @Builder
    private LedgerEntry(LocalDateTime occurredAt, short occurredSeq, EntryType type, LedgerCategory category,
                        String counterparty, String description, String note, long amount, long bankBalance,
                        String sourceFileName, String rowKey, Long createdBy) {
        this.occurredAt = occurredAt;
        this.occurredSeq = occurredSeq;
        this.type = type;
        this.category = category;
        this.counterparty = counterparty;
        this.description = description;
        this.note = note != null ? note : "";
        this.amount = amount;
        this.bankBalance = bankBalance;
        this.sourceFileName = sourceFileName;
        this.rowKey = rowKey;
        this.createdBy = createdBy;
    }

    public void update(String counterparty, LedgerCategory category, String description, String note) {
        this.counterparty = counterparty;
        this.category = category;
        this.description = description;
        this.note = note;
    }

    public void markAsDues() {
        this.category = LedgerCategory.DUES;
    }
}
