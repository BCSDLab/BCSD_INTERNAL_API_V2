package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class ReservationStatusIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 비로그인_상태로_일별_예약을_조회하면_예약자_이름과_목적이_마스킹된다() throws Exception {
        Member member = createActiveMember("20250101");
        LocalDate date = LocalDate.now().plusDays(3);
        reservationRepository.save(Reservation.builder()
                .memberId(member.getId())
                .reservationDate(date)
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("스터디")
                .headcount((short) 4)
                .build());

        mockMvc.perform(get("/v1/reservations/daily").param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations.length()").value(1))
                .andExpect(jsonPath("$.reservations[0].memberName").value(nullValue()))
                .andExpect(jsonPath("$.reservations[0].purpose").value(nullValue()))
                .andExpect(jsonPath("$.reservations[0].mine").value(false))
                .andExpect(jsonPath("$.reservations[0].start").value(600));
    }

    @Test
    void 로그인_상태로_일별_예약을_조회하면_예약자_이름과_목적이_그대로_보인다() throws Exception {
        Member member = createActiveMember("20250102");
        String token = login("20250102");
        LocalDate date = LocalDate.now().plusDays(3);
        reservationRepository.save(Reservation.builder()
                .memberId(member.getId())
                .reservationDate(date)
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("스터디")
                .headcount((short) 4)
                .build());

        mockMvc.perform(get("/v1/reservations/daily")
                        .header("Authorization", "Bearer " + token)
                        .param("date", date.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservations[0].memberName").value(member.getName()))
                .andExpect(jsonPath("$.reservations[0].purpose").value("스터디"))
                .andExpect(jsonPath("$.reservations[0].mine").value(true));
    }

    @Test
    void 월별_예약_현황은_비로그인_상태에서도_조회할_수_있다() throws Exception {
        Member member = createActiveMember("20250103");
        LocalDate date = LocalDate.now().plusDays(3);
        reservationRepository.save(Reservation.builder()
                .memberId(member.getId())
                .reservationDate(date)
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("스터디")
                .headcount((short) 4)
                .build());

        YearMonth month = YearMonth.from(date);
        mockMvc.perform(get("/v1/reservations/monthly-occupancy").param("month", month.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.days[?(@.date == '" + date + "')].reservedMinutes").value(60));
    }
}
