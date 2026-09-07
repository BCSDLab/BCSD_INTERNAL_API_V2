package com.bcsdlab.bcsdinternalapiv2.reservation.repository;

import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r "
            + "where r.memberId = :memberId and r.reservationDate = :date and r.cancelledAt is null")
    List<Reservation> findActiveByMemberIdAndDateForUpdate(@Param("memberId") Long memberId,
                                                            @Param("date") LocalDate date);

    List<Reservation> findByReservationDateAndCancelledAtIsNullOrderByStartMinuteAsc(LocalDate reservationDate);

    List<Reservation> findByReservationGroupIdAndCancelledAtIsNullOrderByReservationDateAsc(Long reservationGroupId);

    List<Reservation> findByReservationGroupIdOrderByReservationDateAsc(Long reservationGroupId);

    List<Reservation> findByMemberIdAndCancelledAtIsNullAndReservationDateGreaterThanEqualOrderByReservationDateAscStartMinuteAsc(
            Long memberId, LocalDate fromDate);

    List<Reservation> findByMemberIdAndCancelledAtIsNullAndReservationDateLessThanOrderByReservationDateDescStartMinuteDesc(
            Long memberId, LocalDate beforeDate);

    @Query("select r.reservationDate, sum(r.endMinute - r.startMinute) from Reservation r "
            + "where r.cancelledAt is null and r.reservationDate between :from and :to "
            + "group by r.reservationDate")
    List<Object[]> sumReservedMinutesByDateBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select r.reservationDate, sum(r.endMinute - r.startMinute) from Reservation r "
            + "where r.memberId = :memberId and r.cancelledAt is null and r.reservationDate between :from and :to "
            + "group by r.reservationDate")
    List<Object[]> sumReservedMinutesByMemberIdAndDateBetween(@Param("memberId") Long memberId,
                                                               @Param("from") LocalDate from,
                                                               @Param("to") LocalDate to);
}
