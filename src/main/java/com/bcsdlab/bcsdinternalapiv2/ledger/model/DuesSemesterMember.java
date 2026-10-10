package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

/**
 * 학기 명단 한 줄. 지우지 않고, 납부 비대상은 applicable=false로 둔다.
 *
 * <p>키를 직접 정하는 엔티티라 {@link Persistable}로 새 행임을 알린다. 그러지 않으면 Spring Data의 save()가
 * merge()를 불러, 이미 있는 행이면 INSERT(PK 위반 → 409) 대신 applicable을 조용히 덮어쓴다.
 */
@Getter
@Entity
@Table(name = "dues_semester_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesSemesterMember extends BaseTimeEntity implements Persistable<DuesSemesterMemberId> {

    @EmbeddedId
    private DuesSemesterMemberId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "applicable", nullable = false)
    private boolean applicable;

    @Transient
    private boolean newRow;

    public DuesSemesterMember(Long semesterId, Member member, boolean applicable) {
        this.id = new DuesSemesterMemberId(semesterId, member.getId());
        this.member = member;
        this.applicable = applicable;
        this.newRow = true;
    }

    public void updateApplicable(boolean applicable) {
        this.applicable = applicable;
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
