package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * 장부 기록 ↔ 증빙(image_asset) 연결. 증빙 하나는 한 기록에만 붙는다(image_asset_id PK).
 * 키를 직접 정하므로 {@link Persistable}로 새 행임을 알린다(그러지 않으면 save()가 merge로 덮어쓴다).
 */
@Getter
@Entity
@Table(name = "ledger_entry_evidence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerEntryEvidence implements Persistable<Long> {

    @Id
    @Column(name = "image_asset_id")
    private Long imageAssetId;

    @Column(name = "entry_id", nullable = false, updatable = false)
    private Long entryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    private boolean newRow;

    public LedgerEntryEvidence(Long imageAssetId, Long entryId, Instant createdAt) {
        this.imageAssetId = imageAssetId;
        this.entryId = entryId;
        this.createdAt = createdAt;
        this.newRow = true;
    }

    @Override
    public Long getId() {
        return imageAssetId;
    }

    @Override
    public boolean isNew() {
        return newRow;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.newRow = false;
    }
}
