package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ReservationDetailIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 본인_예약_상세를_조회할_수_있다() throws Exception {
        String token = createActiveMemberAndLogin("20250501");
        LocalDate date = LocalDate.now().plusDays(3);
        Long reservationId = createSingleReservationAndGetId(token, date);

        mockMvc.perform(get("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.purpose").value("스터디"))
                .andExpect(jsonPath("$.group").value(nullValue()));
    }

    @Test
    void 반복_예약_상세_조회시_그룹_회차_목록이_포함된다() throws Exception {
        String token = createActiveMemberAndLogin("20250502");
        LocalDate anchor = nextDateWithWeekday(DayOfWeek.MONDAY, 3);
        LocalDate endDate = anchor.plusWeeks(2);
        String body = """
                {"date":"%s","start":600,"end":660,"purpose":"반복스터디","headcount":4,
                 "repeat":{"frequency":"WEEKLY","weekdays":["MONDAY"],"endDate":"%s"}}
                """.formatted(anchor, endDate);

        String response = mockMvc.perform(post("/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode created = objectMapper.readTree(response).get("created").get(0);
        long reservationId = created.get("id").asLong();

        mockMvc.perform(get("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.group.occurrences.length()").value(3));
    }

    @Test
    void 타인의_예약을_조회하면_403이_반환된다() throws Exception {
        String ownerToken = createActiveMemberAndLogin("20250503");
        String otherToken = createActiveMemberAndLogin("20250504");
        LocalDate date = LocalDate.now().plusDays(3);
        Long reservationId = createSingleReservationAndGetId(ownerToken, date);

        mockMvc.perform(get("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 존재하지_않는_예약_조회시_404가_반환된다() throws Exception {
        String token = createActiveMemberAndLogin("20250505");

        mockMvc.perform(get("/v1/reservations/{id}", 999_999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private Long createSingleReservationAndGetId(String token, LocalDate date) throws Exception {
        String body = """
                {"date":"%s","start":600,"end":660,"purpose":"스터디","headcount":4}
                """.formatted(date);
        String response = mockMvc.perform(post("/v1/reservations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("created").get(0).get("id").asLong();
    }

    private LocalDate nextDateWithWeekday(DayOfWeek dayOfWeek, int minDaysAhead) {
        LocalDate date = LocalDate.now().plusDays(minDaysAhead);
        while (date.getDayOfWeek() != dayOfWeek) {
            date = date.plusDays(1);
        }
        return date;
    }
}
