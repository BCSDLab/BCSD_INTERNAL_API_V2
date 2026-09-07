package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.LocalDate;
import java.util.List;

public record ReservationCreateResponse(
        List<ReservationDto> created,
        List<SkippedOccurrence> skipped
) {

    public record ReservationDto(Long id, LocalDate date, short start, short end, String purpose, short headcount,
                                  Long groupId) {

        public static ReservationDto from(Reservation reservation) {
            return new ReservationDto(reservation.getId(), reservation.getReservationDate(),
                    reservation.getStartMinute(), reservation.getEndMinute(), reservation.getPurpose(),
                    reservation.getHeadcount(), reservation.getReservationGroupId());
        }
    }

    public record SkippedOccurrence(LocalDate date, String reason) {
    }

    public static ReservationCreateResponse of(List<Reservation> created, List<SkippedOccurrence> skipped) {
        return new ReservationCreateResponse(created.stream().map(ReservationDto::from).toList(), skipped);
    }
}
