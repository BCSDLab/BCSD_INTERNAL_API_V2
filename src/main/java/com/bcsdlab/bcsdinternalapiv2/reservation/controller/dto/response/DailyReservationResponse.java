package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record DailyReservationResponse(
        LocalDate date,
        List<Item> reservations
) {

    public record Item(Long id, short start, short end, String memberName, String purpose, short headcount,
                        boolean mine, Long groupId) {
    }

    public static DailyReservationResponse of(LocalDate date, List<Reservation> reservations,
                                                Map<Long, String> memberNamesById, Long viewerMemberId) {
        boolean loggedIn = viewerMemberId != null;
        List<Item> items = reservations.stream()
                .map(reservation -> new Item(
                        reservation.getId(),
                        reservation.getStartMinute(),
                        reservation.getEndMinute(),
                        loggedIn ? memberNamesById.get(reservation.getMemberId()) : null,
                        loggedIn ? reservation.getPurpose() : null,
                        reservation.getHeadcount(),
                        loggedIn && reservation.isOwnedBy(viewerMemberId),
                        reservation.getReservationGroupId()))
                .toList();
        return new DailyReservationResponse(date, items);
    }
}
