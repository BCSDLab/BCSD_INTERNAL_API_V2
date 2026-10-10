package com.bcsdlab.bcsdinternalapiv2.ledger.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DuesSemesterMemberId implements Serializable {

    @Column(name = "semester_id")
    private Long semesterId;

    @Column(name = "member_id")
    private Long memberId;
}
