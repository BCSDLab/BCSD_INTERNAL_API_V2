package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "dues_semester")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesSemester extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year", nullable = false, updatable = false)
    private short year;

    @Column(name = "term", nullable = false, updatable = false)
    private short term;

    @Column(name = "monthly_amount", nullable = false)
    private long monthlyAmount;

    @Column(name = "created_by", updatable = false)
    private Long createdBy;

    public DuesSemester(SemesterKey key, long monthlyAmount, Long createdBy) {
        this.year = (short) key.year();
        this.term = (short) key.term();
        this.monthlyAmount = monthlyAmount;
        this.createdBy = createdBy;
    }

    public SemesterKey key() {
        return new SemesterKey(year, term);
    }
}
