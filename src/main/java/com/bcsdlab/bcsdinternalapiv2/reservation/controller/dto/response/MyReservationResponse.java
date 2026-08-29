package com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.LocalDate;
import java.util.List;

public record MyReservationResponse(
        List<Item> reservations
) {

    public record Item(Long id, LocalDate date, short start, short end, String purpose, short headcount,
                        Long groupId, boolean repeating) {

        public static Item from(Reservation reservation) {
            return new Item(reservation.getId(), reservation.getReservationDate(), reservation.getStartMinute(),
                    reservation.getEndMinute(), reservation.getPurpose(), reservation.getHeadcount(),
                    reservation.getReservationGroupId(), reservation.belongsToGroup());
        }
    }

    public static MyReservationResponse of(List<Reservation> representatives) {
        return new MyReservationResponse(representatives.stream().map(Item::from).toList());
    }
}
