package com.bcsdlab.bcsdinternalapiv2.reservation.model;

import com.bcsdlab.bcsdinternalapiv2.global.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reservation")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "reservation_group_id")
    private Long reservationGroupId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "start_minute", nullable = false)
    private short startMinute;

    @Column(name = "end_minute", nullable = false)
    private short endMinute;

    @Column(name = "purpose", nullable = false)
    private String purpose;

    @Column(name = "headcount", nullable = false)
    private short headcount;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Builder
    private Reservation(Long memberId, Long reservationGroupId, LocalDate reservationDate, short startMinute,
                         short endMinute, String purpose, short headcount) {
        this.memberId = memberId;
        this.reservationGroupId = reservationGroupId;
        this.reservationDate = reservationDate;
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.purpose = purpose;
        this.headcount = headcount;
    }

    public boolean isCancelled() {
        return this.cancelledAt != null;
    }

    public boolean isOwnedBy(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean belongsToGroup() {
        return this.reservationGroupId != null;
    }

    public int durationMinutes() {
        return this.endMinute - this.startMinute;
    }

    public void cancel(Instant now) {
        this.cancelledAt = now;
    }
}
