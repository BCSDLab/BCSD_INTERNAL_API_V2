package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.RepeatFrequency;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.ReservationGroup;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record ReservationDetailResponse(
        Long id,
        LocalDate date,
        short start,
        short end,
        String purpose,
        short headcount,
        Instant cancelledAt,
        Group group
) {

    public record Occurrence(Long id, LocalDate date, boolean cancelled) {

        public static Occurrence from(Reservation reservation) {
            return new Occurrence(reservation.getId(), reservation.getReservationDate(), reservation.isCancelled());
        }
    }

    public record Group(Long id, RepeatFrequency frequency, List<DayOfWeek> weekdays, LocalDate endDate,
                         List<Occurrence> occurrences) {

        public static Group of(ReservationGroup group, List<Reservation> occurrences) {
            return new Group(group.getId(), group.getFrequency(), group.getWeekdayList(), group.getRepeatEndDate(),
                    occurrences.stream().map(Occurrence::from).toList());
        }
    }

    public static ReservationDetailResponse of(Reservation reservation, Group group) {
        return new ReservationDetailResponse(reservation.getId(), reservation.getReservationDate(),
                reservation.getStartMinute(), reservation.getEndMinute(), reservation.getPurpose(),
                reservation.getHeadcount(), reservation.getCancelledAt(), group);
    }
}
