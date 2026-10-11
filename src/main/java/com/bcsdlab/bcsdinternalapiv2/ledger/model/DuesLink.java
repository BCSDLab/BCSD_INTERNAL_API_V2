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
 * 장부 기록 ↔ (학기, 회원) 연결. 기록 하나에 연결 하나(entry_id PK)이고 스냅샷은 두지 않는다.
 *
 * <p>다른 회원으로 옮길 때는 행을 지우고 새로 넣지 않고 이 행을 고친다(Hibernate는 flush 때 INSERT를
 * DELETE보다 먼저 실행해 PK 위반이 난다). 키를 직접 정하므로 {@link Persistable}로 새 행임을 알린다.
 */
@Getter
@Entity
@Table(name = "dues_link")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesLink implements Persistable<Long> {

    @Id
    @Column(name = "entry_id")
    private Long entryId;

    @Column(name = "semester_id", nullable = false)
    private Long semesterId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "linked_by")
    private Long linkedBy;

    @Column(name = "linked_at", nullable = false)
    private Instant linkedAt;

    @Transient
    private boolean newRow;

    public DuesLink(Long entryId, Long semesterId, Long memberId, Long linkedBy, Instant linkedAt) {
        this.entryId = entryId;
        this.semesterId = semesterId;
        this.memberId = memberId;
        this.linkedBy = linkedBy;
        this.linkedAt = linkedAt;
        this.newRow = true;
    }

    public void moveTo(Long semesterId, Long memberId, Long linkedBy, Instant linkedAt) {
        this.semesterId = semesterId;
        this.memberId = memberId;
        this.linkedBy = linkedBy;
        this.linkedAt = linkedAt;
    }

    @Override
    public Long getId() {
        return entryId;
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
