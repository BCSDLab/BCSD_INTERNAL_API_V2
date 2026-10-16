package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.MutableClock;
import com.bcsdlab.bcsdinternalapiv2.global.config.TimeConfig;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

/**
 * 예약의 "지금"은 서버 시간대가 아니라 한국 시간이다. 운영 컨테이너는 UTC라서, 시스템 기본 시간대를 쓰면
 * 한국 시각 기준으로 이미 지난 시간을 최대 9시간까지 예약·취소할 수 있었다.
 */
@Import(ReservationTimeZoneIntegrationTest.TestClockConfig.class)
class ReservationTimeZoneIntegrationTest extends ReservationIntegrationTestSupport {

    private static final int NINE = 9 * 60;
    private static final int TEN = 10 * 60;
    private static final int ELEVEN = 11 * 60;

    @Autowired
    private MutableClock clock;

    private LocalDate today;

    @BeforeEach
    void setUpClock() {
        today = LocalDate.now(TimeConfig.SEOUL).plusDays(1);
        clock.setInstant(today.atTime(9, 30).atZone(TimeConfig.SEOUL).toInstant());
    }

    @Test
    void 한국_시각으로_이미_지난_시간은_예약할_수_없다() throws Exception {
        String token = createActiveMemberAndLogin("20250501");

        createReservation(token, today, NINE, TEN)
                .andExpect(status().isBadRequest());
    }

    @Test
    void 한국_시각으로_아직_오지_않은_시간은_예약할_수_있다() throws Exception {
        String token = createActiveMemberAndLogin("20250502");

        createReservation(token, today, TEN, ELEVEN)
                .andExpect(status().isOk());
    }

    @Test
    void 한국_시각으로_이미_시작한_예약은_취소할_수_없다() throws Exception {
        String token = createActiveMemberAndLogin("20250503");
        createReservation(token, today, TEN, ELEVEN).andExpect(status().isOk());
        Long reservationId = reservationRepository.findAll().getFirst().getId();

        clock.setInstant(today.atTime(10, 30).atZone(TimeConfig.SEOUL).toInstant());

        mockMvc.perform(delete("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private ResultActions createReservation(String token, LocalDate date, int start, int end) throws Exception {
        return mockMvc.perform(post("/v1/reservations")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"date":"%s","start":%d,"end":%d,"purpose":"스터디","headcount":4}
                        """.formatted(date, start, end)));
    }

    @TestConfiguration
    static class TestClockConfig {

        @Bean
        @Primary
        MutableClock testClock() {
            return new MutableClock();
        }
    }
}
