package com.bcsdlab.bcsdinternalapiv2.reservation.service;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.member.repository.MemberRepository;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.request.ReservationCreateRequest;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.DailyReservationResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MonthlyOccupancyResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.MyReservationResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationCreateResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationCreateResponse.SkippedOccurrence;
import com.bcsdlab.bcsdinternalapiv2.reservation.controller.dto.response.ReservationDetailResponse;
import com.bcsdlab.bcsdinternalapiv2.reservation.exception.ReservationException;
import com.bcsdlab.bcsdinternalapiv2.reservation.exception.ReservationExceptionType;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.ReservationGroup;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.RepeatFrequency;
import com.bcsdlab.bcsdinternalapiv2.reservation.repository.ReservationGroupRepository;
import com.bcsdlab.bcsdinternalapiv2.reservation.repository.ReservationRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private static final int MAX_DAILY_MINUTES = 180;
    private static final int MAX_HEADCOUNT = 12;
    private static final int SINGLE_BOOKING_WINDOW_DAYS = 14;
    private static final int REPEAT_WINDOW_DAYS = 84;

    private final ReservationRepository reservationRepository;
    private final ReservationGroupRepository reservationGroupRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReservationCreateResponse create(Long memberId, ReservationCreateRequest request) {
        Instant now = Instant.now();
        validateCommon(request, now);

        if (!request.isRepeat()) {
            checkDailyLimitOrThrow(memberId, request.date(), request.start(), request.end());
            checkOverlapOrThrow(request.date(), request.start(), request.end());
            Reservation reservation = saveOrTranslateConflict(buildReservation(memberId, null, request.date(),
                    request.start(), request.end(), request.purpose(), request.headcount()));
            return ReservationCreateResponse.of(List.of(reservation), List.of());
        }

        return createRepeat(memberId, request, now);
    }

    @Transactional(readOnly = true)
    public DailyReservationResponse getDaily(LocalDate date, Long viewerMemberId) {
        List<Reservation> reservations =
                reservationRepository.findByReservationDateAndCancelledAtIsNullOrderByStartMinuteAsc(date);
        Map<Long, String> namesById = viewerMemberId == null ? Map.of() : loadMemberNames(reservations);
        return DailyReservationResponse.of(date, reservations, namesById, viewerMemberId);
    }

    @Transactional(readOnly = true)
    public MonthlyOccupancyResponse getMonthlyOccupancy(YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        return MonthlyOccupancyResponse.of(month, toDays(reservationRepository.sumReservedMinutesByDateBetween(from, to)));
    }

    @Transactional(readOnly = true)
    public MonthlyOccupancyResponse getMyMonthlyOccupancy(Long memberId, YearMonth month) {
        LocalDate from = month.atDay(1);
        LocalDate to = month.atEndOfMonth();
        List<Object[]> rows = reservationRepository.sumReservedMinutesByMemberIdAndDateBetween(memberId, from, to);
        return MonthlyOccupancyResponse.of(month, toDays(rows));
    }

    @Transactional(readOnly = true)
    public MyReservationResponse getMyReservations(Long memberId, String status) {
        LocalDate today = LocalDate.now();
        List<Reservation> reservations = "past".equalsIgnoreCase(status)
                ? reservationRepository
                        .findByMemberIdAndCancelledAtIsNullAndReservationDateLessThanOrderByReservationDateDescStartMinuteDesc(
                                memberId, today)
                : reservationRepository
                        .findByMemberIdAndCancelledAtIsNullAndReservationDateGreaterThanEqualOrderByReservationDateAscStartMinuteAsc(
                                memberId, today);
        return MyReservationResponse.of(pickGroupRepresentatives(reservations));
    }

    @Transactional(readOnly = true)
    public ReservationDetailResponse getDetail(Long memberId, Long reservationId) {
        Reservation reservation = getOwnedReservation(memberId, reservationId);
        ReservationDetailResponse.Group group = null;
        if (reservation.belongsToGroup()) {
            ReservationGroup reservationGroup = reservationGroupRepository.findById(reservation.getReservationGroupId())
                    .orElseThrow(() -> new ReservationException(ReservationExceptionType.RESERVATION_GROUP_NOT_FOUND));
            List<Reservation> occurrences = reservationRepository
                    .findByReservationGroupIdOrderByReservationDateAsc(reservation.getReservationGroupId());
            group = ReservationDetailResponse.Group.of(reservationGroup, occurrences);
        }
        return ReservationDetailResponse.of(reservation, group);
    }

    @Transactional
    public void cancel(Long memberId, Long reservationId) {
        Instant now = Instant.now();
        Reservation reservation = getOwnedReservation(memberId, reservationId);
        if (reservation.isCancelled()) {
            throw new ReservationException(ReservationExceptionType.ALREADY_CANCELLED);
        }
        if (hasStarted(reservation, now)) {
            throw new ReservationException(ReservationExceptionType.CANCEL_DEADLINE_PASSED);
        }
        reservation.cancel(now);
    }

    @Transactional
    public void cancelGroup(Long memberId, Long groupId) {
        Instant now = Instant.now();
        ReservationGroup group = reservationGroupRepository.findById(groupId)
                .orElseThrow(() -> new ReservationException(ReservationExceptionType.RESERVATION_GROUP_NOT_FOUND));
        if (!group.getMemberId().equals(memberId)) {
            throw new ReservationException(ReservationExceptionType.NOT_RESERVATION_OWNER);
        }
        List<Reservation> occurrences =
                reservationRepository.findByReservationGroupIdAndCancelledAtIsNullOrderByReservationDateAsc(groupId);
        for (Reservation occurrence : occurrences) {
            if (!hasStarted(occurrence, now)) {
                occurrence.cancel(now);
            }
        }
    }

    private ReservationCreateResponse createRepeat(Long memberId, ReservationCreateRequest request, Instant now) {
        ReservationCreateRequest.RepeatOption repeat = request.repeat();
        ReservationGroup group = reservationGroupRepository.save(ReservationGroup.builder()
                .memberId(memberId)
                .frequency(repeat.frequency())
                .weekdays(repeat.weekdays())
                .startMinute(request.start())
                .endMinute(request.end())
                .repeatEndDate(repeat.endDate())
                .createdAt(now)
                .build());

        List<Reservation> created = new ArrayList<>();
        List<SkippedOccurrence> skipped = new ArrayList<>();
        for (LocalDate date : generateOccurrenceDates(request.date(), repeat.frequency(), repeat.weekdays(), repeat.endDate())) {
            Optional<ReservationExceptionType> conflict = checkAvailability(memberId, date, request.start(), request.end());
            if (conflict.isPresent()) {
                skipped.add(new SkippedOccurrence(date, conflict.get().name()));
                continue;
            }
            Reservation reservation = buildReservation(memberId, group.getId(), date, request.start(), request.end(),
                    request.purpose(), request.headcount());
            created.add(reservationRepository.save(reservation));
        }
        return ReservationCreateResponse.of(created, skipped);
    }

    private List<LocalDate> generateOccurrenceDates(LocalDate anchorDate, RepeatFrequency frequency,
                                                     List<DayOfWeek> weekdays, LocalDate endDateInclusive) {
        int stepWeeks = frequency == RepeatFrequency.BIWEEKLY ? 2 : 1;
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate cursor = anchorDate; !cursor.isAfter(endDateInclusive); cursor = cursor.plusDays(1)) {
            if (!weekdays.contains(cursor.getDayOfWeek())) {
                continue;
            }
            long weekIndex = Math.floorDiv(ChronoUnit.DAYS.between(anchorDate, cursor), 7);
            if (weekIndex % stepWeeks == 0) {
                dates.add(cursor);
            }
        }
        return dates;
    }

    private void validateCommon(ReservationCreateRequest request, Instant now) {
        short start = request.start();
        short end = request.end();
        if (start >= end) {
            throw new ReservationException(ReservationExceptionType.INVALID_TIME_RANGE);
        }
        if (start % 30 != 0 || end % 30 != 0) {
            throw new ReservationException(ReservationExceptionType.SLOT_NOT_ALIGNED);
        }
        if (request.headcount() > MAX_HEADCOUNT) {
            throw new ReservationException(ReservationExceptionType.INVALID_HEADCOUNT);
        }
        LocalDateTime startDateTime = request.date().atStartOfDay().plusMinutes(start);
        if (!startDateTime.isAfter(LocalDateTime.ofInstant(now, ZoneId.systemDefault()))) {
            throw new ReservationException(ReservationExceptionType.PAST_TIME);
        }

        LocalDate today = LocalDate.now();
        if (!request.isRepeat()) {
            if (request.date().isAfter(today.plusDays(SINGLE_BOOKING_WINDOW_DAYS))) {
                throw new ReservationException(ReservationExceptionType.OUT_OF_SINGLE_BOOKING_WINDOW);
            }
            return;
        }

        ReservationCreateRequest.RepeatOption repeat = request.repeat();
        if (!repeat.weekdays().contains(request.date().getDayOfWeek())) {
            throw new ReservationException(ReservationExceptionType.INVALID_WEEKDAYS);
        }
        if (!repeat.endDate().isAfter(request.date())) {
            throw new ReservationException(ReservationExceptionType.INVALID_TIME_RANGE);
        }
        if (repeat.endDate().isAfter(request.date().plusDays(REPEAT_WINDOW_DAYS))) {
            throw new ReservationException(ReservationExceptionType.OUT_OF_REPEAT_WINDOW);
        }
    }

    private void checkDailyLimitOrThrow(Long memberId, LocalDate date, short start, short end) {
        if (dailyLimitExceeded(memberId, date, start, end)) {
            throw new ReservationException(ReservationExceptionType.DAILY_LIMIT_EXCEEDED);
        }
    }

    private void checkOverlapOrThrow(LocalDate date, short start, short end) {
        if (overlapsExisting(date, start, end)) {
            throw new ReservationException(ReservationExceptionType.OVERLAPPING_RESERVATION);
        }
    }

    private Optional<ReservationExceptionType> checkAvailability(Long memberId, LocalDate date, short start, short end) {
        if (overlapsExisting(date, start, end)) {
            return Optional.of(ReservationExceptionType.OVERLAPPING_RESERVATION);
        }
        if (dailyLimitExceeded(memberId, date, start, end)) {
            return Optional.of(ReservationExceptionType.DAILY_LIMIT_EXCEEDED);
        }
        return Optional.empty();
    }

    private boolean overlapsExisting(LocalDate date, short start, short end) {
        return reservationRepository.findByReservationDateAndCancelledAtIsNullOrderByStartMinuteAsc(date).stream()
                .anyMatch(existing -> existing.getStartMinute() < end && start < existing.getEndMinute());
    }

    private boolean dailyLimitExceeded(Long memberId, LocalDate date, short start, short end) {
        int existingMinutes = reservationRepository.findActiveByMemberIdAndDateForUpdate(memberId, date).stream()
                .mapToInt(Reservation::durationMinutes)
                .sum();
        return existingMinutes + (end - start) > MAX_DAILY_MINUTES;
    }

    private Reservation buildReservation(Long memberId, Long groupId, LocalDate date, short start, short end,
                                          String purpose, short headcount) {
        return Reservation.builder()
                .memberId(memberId)
                .reservationGroupId(groupId)
                .reservationDate(date)
                .startMinute(start)
                .endMinute(end)
                .purpose(purpose)
                .headcount(headcount)
                .build();
    }

    private Reservation saveOrTranslateConflict(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw new ReservationException(ReservationExceptionType.OVERLAPPING_RESERVATION);
        }
    }

    private Reservation getOwnedReservation(Long memberId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationExceptionType.RESERVATION_NOT_FOUND));
        if (!reservation.isOwnedBy(memberId)) {
            throw new ReservationException(ReservationExceptionType.NOT_RESERVATION_OWNER);
        }
        return reservation;
    }

    private boolean hasStarted(Reservation reservation, Instant now) {
        LocalDateTime startDateTime = reservation.getReservationDate().atStartOfDay()
                .plusMinutes(reservation.getStartMinute());
        return !startDateTime.isAfter(LocalDateTime.ofInstant(now, ZoneId.systemDefault()));
    }

    private List<Reservation> pickGroupRepresentatives(List<Reservation> reservations) {
        List<Reservation> result = new ArrayList<>();
        Set<Long> seenGroupIds = new HashSet<>();
        for (Reservation reservation : reservations) {
            if (!reservation.belongsToGroup()) {
                result.add(reservation);
            } else if (seenGroupIds.add(reservation.getReservationGroupId())) {
                result.add(reservation);
            }
        }
        return result;
    }

    private Map<Long, String> loadMemberNames(List<Reservation> reservations) {
        List<Long> memberIds = reservations.stream().map(Reservation::getMemberId).distinct().toList();
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Member::getName));
    }

    private List<MonthlyOccupancyResponse.Day> toDays(List<Object[]> rows) {
        return rows.stream()
                .map(row -> new MonthlyOccupancyResponse.Day((LocalDate) row[0], ((Number) row[1]).intValue()))
                .toList();
    }
}
