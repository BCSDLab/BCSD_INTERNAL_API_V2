package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회비 면제. 학기에 묶이지 않고 시작 월 ~ 종료 월(null이면 계속)로 저장한다. 겹쳐도 되고 우선순위는 없다 —
 * 어떤 달에 하나라도 걸리면 그 달은 면제다.
 */
@Getter
@Entity
@Table(name = "dues_exemption")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesExemption extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "note", nullable = false)
    private String note;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "start_month", nullable = false)
    private YearMonth startMonth;

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "end_month")
    private YearMonth endMonth;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    public DuesExemption(Long memberId, String reason, String note, YearMonth startMonth, YearMonth endMonth,
                         Long createdBy) {
        this.memberId = memberId;
        this.reason = reason;
        this.note = note;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.createdBy = createdBy;
    }

    public void update(Long memberId, String reason, String note, YearMonth startMonth, YearMonth endMonth) {
        this.memberId = memberId;
        this.reason = reason;
        this.note = note;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
    }
}
