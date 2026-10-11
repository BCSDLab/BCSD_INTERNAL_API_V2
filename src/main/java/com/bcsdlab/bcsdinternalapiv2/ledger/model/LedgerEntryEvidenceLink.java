package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장부 기록에 붙은 증빙 링크(구글 드라이브). 관리자가 드라이브에 직접 올린 뒤 링크만 붙인다.
 * 같은 기록에 같은 url은 한 번만 붙는다((entry_id, url) 유니크).
 */
@Getter
@Entity
@Table(name = "ledger_entry_evidence_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntryEvidenceLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entry_id", nullable = false, updatable = false)
    private Long entryId;

    @Column(nullable = false, updatable = false, length = 2048)
    private String url;

    @Column(length = 50)
    private String label;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public LedgerEntryEvidenceLink(Long entryId, String url, String label, Instant createdAt) {
        this.entryId = entryId;
        this.url = url;
        this.label = label;
        this.createdAt = createdAt;
    }

    public void relabel(String label) {
        this.label = label;
    }
}
