package com.bcsdlab.bcsdinternalapiv2.reservation.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "reservation_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationGroup {

    private static final String WEEKDAY_DELIMITER = ",";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false)
    private RepeatFrequency frequency;

    @Column(name = "weekdays", nullable = false)
    private String weekdays;

    @Column(name = "start_minute", nullable = false)
    private short startMinute;

    @Column(name = "end_minute", nullable = false)
    private short endMinute;

    @Column(name = "repeat_end_date", nullable = false)
    private LocalDate repeatEndDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Builder
    private ReservationGroup(Long memberId, RepeatFrequency frequency, List<DayOfWeek> weekdays,
                              short startMinute, short endMinute, LocalDate repeatEndDate, Instant createdAt) {
        this.memberId = memberId;
        this.frequency = frequency;
        this.weekdays = weekdays.stream().map(Enum::name).collect(Collectors.joining(WEEKDAY_DELIMITER));
        this.startMinute = startMinute;
        this.endMinute = endMinute;
        this.repeatEndDate = repeatEndDate;
        this.createdAt = createdAt;
    }

    public List<DayOfWeek> getWeekdayList() {
        return Arrays.stream(weekdays.split(WEEKDAY_DELIMITER))
                .map(DayOfWeek::valueOf)
                .toList();
    }

    public boolean includesWeekday(DayOfWeek dayOfWeek) {
        return getWeekdayList().contains(dayOfWeek);
    }
}
