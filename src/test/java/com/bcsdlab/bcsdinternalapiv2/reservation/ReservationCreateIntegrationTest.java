package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ReservationCreateIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 로그인한_회원은_단건_예약을_생성할_수_있다() throws Exception {
        String token = createActiveMemberAndLogin("20250201");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(token, date, 600, 660, "스터디", 4)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.created.length()").value(1))
                .andExpect(jsonPath("$.created[0].purpose").value("스터디"))
                .andExpect(jsonPath("$.skipped.length()").value(0));
    }

    @Test
    void 시간이_30분_단위가_아니면_예약이_거부된다() throws Exception {
        String token = createActiveMemberAndLogin("20250202");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(token, date, 601, 660, "스터디", 4)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 시작시간이_종료시간보다_늦으면_예약이_거부된다() throws Exception {
        String token = createActiveMemberAndLogin("20250203");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(token, date, 660, 600, "스터디", 4)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이미_지난_시간은_예약할_수_없다() throws Exception {
        String token = createActiveMemberAndLogin("20250204");
        LocalDate date = LocalDate.now().minusDays(1);

        createReservation(token, date, 600, 660, "스터디", 4)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 이주를_초과한_날짜는_예약할_수_없다() throws Exception {
        String token = createActiveMemberAndLogin("20250205");
        LocalDate date = LocalDate.now().plusDays(20);

        createReservation(token, date, 600, 660, "스터디", 4)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 인원수가_한도를_초과하면_예약이_거부된다() throws Exception {
        String token = createActiveMemberAndLogin("20250206");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(token, date, 600, 660, "스터디", 13)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 겹치는_시간대는_예약할_수_없다() throws Exception {
        String firstToken = createActiveMemberAndLogin("20250207");
        String secondToken = createActiveMemberAndLogin("20250208");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(firstToken, date, 600, 660, "스터디", 4)
                .andExpect(status().isOk());

        createReservation(secondToken, date, 630, 690, "회의", 2)
                .andExpect(status().isConflict());
    }

    @Test
    void 하루_3시간_한도를_초과하면_예약할_수_없다() throws Exception {
        String token = createActiveMemberAndLogin("20250209");
        LocalDate date = LocalDate.now().plusDays(3);

        createReservation(token, date, 540, 600, "스터디1", 2).andExpect(status().isOk());
        createReservation(token, date, 600, 660, "스터디2", 2).andExpect(status().isOk());
        createReservation(token, date, 660, 720, "스터디3", 2).andExpect(status().isOk());

        createReservation(token, date, 720, 750, "스터디4", 2)
                .andExpect(status().isConflict());
    }

    @Test
    void 비로그인_요청은_401이_반환된다() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        mockMvc.perform(post("/v1/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody(date, 600, 660, "스터디", 4)))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions createReservation(String token, LocalDate date, int start, int end, String purpose,
                                             int headcount) throws Exception {
        return mockMvc.perform(post("/v1/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody(date, start, end, purpose, headcount)));
    }

    private String requestBody(LocalDate date, int start, int end, String purpose, int headcount) {
        return """
                {"date":"%s","start":%d,"end":%d,"purpose":"%s","headcount":%d}
                """.formatted(date, start, end, purpose, headcount);
    }
}
