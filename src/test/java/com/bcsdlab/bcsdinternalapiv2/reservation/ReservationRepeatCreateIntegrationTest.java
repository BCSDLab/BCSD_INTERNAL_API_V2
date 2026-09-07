package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ReservationRepeatCreateIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 반복_예약을_생성하면_선택한_요일마다_회차가_생성된다() throws Exception {
        String token = createActiveMemberAndLogin("20250301");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusWeeks(2);

        createRepeatReservation(token, anchor, 600, 660, "MONDAY", endDate)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created.length()").value(3))
                .andExpect(jsonPath("$.skipped.length()").value(0));
    }

    @Test
    void 반복_구간에_겹치는_날짜가_있으면_해당_회차만_건너뛰고_나머지는_생성된다() throws Exception {
        Member other = createActiveMember("20250302");
        String token = createActiveMemberAndLogin("20250303");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate conflictingDate = anchor.plusWeeks(1);
        LocalDate endDate = anchor.plusWeeks(2);

        reservationRepository.save(Reservation.builder()
                .memberId(other.getId())
                .reservationDate(conflictingDate)
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("선점")
                .headcount((short) 2)
                .build());

        createRepeatReservation(token, anchor, 600, 660, "MONDAY", endDate)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created.length()").value(2))
                .andExpect(jsonPath("$.skipped.length()").value(1))
                .andExpect(jsonPath("$.skipped[0].date").value(conflictingDate.toString()))
                .andExpect(jsonPath("$.skipped[0].reason").value("OVERLAPPING_RESERVATION"));
    }

    @Test
    void 반복_요일에_시작일의_요일이_포함되지_않으면_예약이_거부된다() throws Exception {
        String token = createActiveMemberAndLogin("20250304");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusWeeks(2);

        createRepeatReservation(token, anchor, 600, 660, "TUESDAY", endDate)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 반복_종료일이_12주를_초과하면_예약이_거부된다() throws Exception {
        String token = createActiveMemberAndLogin("20250305");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusDays(100);

        createRepeatReservation(token, anchor, 600, 660, "MONDAY", endDate)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 격주로_반복_예약을_생성하면_2주_간격으로만_회차가_생성된다() throws Exception {
        String token = createActiveMemberAndLogin("20250306");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusWeeks(6);

        String body = """
                {"date":"%s","start":600,"end":660,"purpose":"격주스터디","headcount":4,
                 "repeat":{"frequency":"BIWEEKLY","weekdays":["MONDAY"],"endDate":"%s"}}
                """.formatted(anchor, endDate);

        mockMvc.perform(post("/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created.length()").value(4))
                .andExpect(jsonPath("$.created[0].date").value(anchor.toString()))
                .andExpect(jsonPath("$.created[1].date").value(anchor.plusWeeks(2).toString()))
                .andExpect(jsonPath("$.created[2].date").value(anchor.plusWeeks(4).toString()))
                .andExpect(jsonPath("$.created[3].date").value(anchor.plusWeeks(6).toString()));
    }

    private LocalDate nextDateWithWeekday(DayOfWeek dayOfWeek, int minDaysAhead) {
        LocalDate date = LocalDate.now().plusDays(minDaysAhead);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }

    private ResultActions createRepeatReservation(String token, LocalDate date, int start, int end, String weekday,
                                                   LocalDate endDate) throws Exception {
        String body = """
                {"date":"%s","start":%d,"end":%d,"purpose":"스터디","headcount":4,
                 "repeat":{"frequency":"WEEKLY","weekdays":["%s"],"endDate":"%s"}}
                """.formatted(date, start, end, weekday, endDate);
        return mockMvc.perform(post("/v1/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
