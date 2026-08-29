package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class MyReservationIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 예정된_예약_목록을_조회할_수_있다() throws Exception {
        String token = createActiveMemberAndLogin("20250401");
        LocalDate date = LocalDate.now().plusDays(3);
        mockMvc.perform(post("/v1/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(singleRequestBody(date)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/reservations/me")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].date").value(date.toString()));
    }

    @Test
    void 지난_예약_목록을_조회할_수_있다() throws Exception {
        Member member = createActiveMember("20250402");
        String token = login("20250402");
        reservationRepository.save(Reservation.builder()
                .memberId(member.getId())
                .reservationDate(LocalDate.now().minusDays(3))
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("지난스터디")
                .headcount((short) 2)
                .build());

        mockMvc.perform(get("/v1/reservations/me")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "past"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].purpose").value("지난스터디"));
    }

    @Test
    void 반복_예약은_대표_1건만_목록에_포함된다() throws Exception {
        String token = createActiveMemberAndLogin("20250403");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusWeeks(2);
        String body = """
                {"date":"%s","start":600,"end":660,"purpose":"반복스터디","headcount":4,
                 "repeat":{"frequency":"WEEKLY","weekdays":["MONDAY"],"endDate":"%s"}}
                """.formatted(anchor, endDate);

        mockMvc.perform(post("/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/reservations/me")
                        .header("Authorization", "Bearer " + token)
                        .param("status", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].repeating").value(true))
                .andExpect(jsonPath("$.reservations[0].date").value(anchor.toString()));
    }

    @Test
    void 다른_회원의_예약은_조회되지_않는다() throws Exception {
        String ownerToken = createActiveMemberAndLogin("20250404");
        String otherToken = createActiveMemberAndLogin("20250405");
        LocalDate date = LocalDate.now().plusDays(3);

        mockMvc.perform(post("/v1/reservations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(singleRequestBody(date)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/v1/reservations/me")
                        .header("Authorization", "Bearer " + otherToken)
                        .param("status", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(0));
    }

    private LocalDate nextDateWithWeekday(DayOfWeek dayOfWeek, int minDaysAhead) {
        LocalDate date = LocalDate.now().plusDays(minDaysAhead);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }

    private String singleRequestBody(LocalDate date) {
        return """
                {"date":"%s","start":600,"end":660,"purpose":"스터디","headcount":4}
                """.formatted(date);
    }
}
