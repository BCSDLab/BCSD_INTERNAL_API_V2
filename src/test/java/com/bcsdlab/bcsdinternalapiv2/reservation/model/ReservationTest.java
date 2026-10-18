package com.bcsdlab.bcsdinternalapiv2.reservation.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReservationTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    @ParameterizedTest(name = "10:00~11:00 vs [{0}, {1}) → {2}")
    @CsvSource({
            "540, 600, false",
            "540, 601, true",
            "600, 660, true",
            "630, 690, true",
            "570, 720, true",
            "660, 720, false",
    })
    void 분_구간이_겹치는지_판단한다(short start, short end, boolean expected) {
        Reservation reservation = reservation((short) 600, (short) 660);

        assertThat(reservation.overlaps(start, end)).isEqualTo(expected);
    }

    @Test
    void 시작_시각_전이면_시작하지_않았다() {
        Reservation reservation = reservation((short) 600, (short) 660);

        assertThat(reservation.hasStartedAt(DATE.atTime(9, 59, 59))).isFalse();
    }

    @Test
    void 시작_시각과_같거나_지나면_시작했다() {
        Reservation reservation = reservation((short) 600, (short) 660);

        assertThat(reservation.hasStartedAt(DATE.atTime(10, 0))).isTrue();
        assertThat(reservation.hasStartedAt(LocalDateTime.of(DATE.plusDays(1), LocalTime.MIDNIGHT))).isTrue();
    }

    @Test
    void 길이는_끝에서_시작을_뺀_분이다() {
        assertThat(reservation((short) 600, (short) 690).durationMinutes()).isEqualTo(90);
    }

    private static Reservation reservation(short start, short end) {
        return Reservation.builder()
                .memberId(1L)
                .reservationDate(DATE)
                .startMinute(start)
                .endMinute(end)
                .purpose("스터디")
                .headcount((short) 3)
                .build();
    }
}
