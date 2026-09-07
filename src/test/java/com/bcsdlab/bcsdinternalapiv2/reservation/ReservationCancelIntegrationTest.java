package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bcsdlab.bcsdinternalapiv2.member.model.Member;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.Reservation;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.ReservationGroup;
import com.bcsdlab.bcsdinternalapiv2.reservation.model.RepeatFrequency;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ReservationCancelIntegrationTest extends ReservationIntegrationTestSupport {

    @Test
    void 본인_예약을_취소할_수_있다() throws Exception {
        String token = createActiveMemberAndLogin("20250601");
        LocalDate date = LocalDate.now().plusDays(3);
        Long reservationId = createSingleReservationAndGetId(token, date);

        mockMvc.perform(delete("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(reservationRepository.findById(reservationId).orElseThrow().isCancelled()).isTrue();
    }

    @Test
    void 이미_취소된_예약을_다시_취소하면_409가_반환된다() throws Exception {
        String token = createActiveMemberAndLogin("20250602");
        LocalDate date = LocalDate.now().plusDays(3);
        Long reservationId = createSingleReservationAndGetId(token, date);

        mockMvc.perform(delete("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void 타인의_예약을_취소하면_403이_반환된다() throws Exception {
        String ownerToken = createActiveMemberAndLogin("20250603");
        String otherToken = createActiveMemberAndLogin("20250604");
        LocalDate date = LocalDate.now().plusDays(3);
        Long reservationId = createSingleReservationAndGetId(ownerToken, date);

        mockMvc.perform(delete("/v1/reservations/{id}", reservationId)
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void 이미_시작된_예약은_취소할_수_없다() throws Exception {
        Member member = createActiveMember("20250605");
        String token = login("20250605");
        Reservation started = reservationRepository.save(Reservation.builder()
                .memberId(member.getId())
                .reservationDate(LocalDate.now().minusDays(1))
                .startMinute((short) 600)
                .endMinute((short) 660)
                .purpose("지난예약")
                .headcount((short) 2)
                .build());

        mockMvc.perform(delete("/v1/reservations/{id}", started.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    @Test
    void 반복_예약_그룹을_전체_취소하면_미래_회차만_취소된다() throws Exception {
        Member member = createActiveMember("20250606");
        String token = login("20250606");
        LocalDate pastDate = LocalDate.now().minusDays(7);
        LocalDate futureDate1 = LocalDate.now().plusDays(3);
        LocalDate futureDate2 = LocalDate.now().plusDays(10);

        ReservationGroup group = reservationGroupRepository.save(ReservationGroup.builder()
                .memberId(member.getId())
                .frequency(RepeatFrequency.WEEKLY)
                .weekdays(List.of(pastDate.getDayOfWeek()))
                .startMinute((short) 600)
                .endMinute((short) 660)
                .repeatEndDate(futureDate2)
                .createdAt(java.time.Instant.now())
                .build());

        Reservation pastOccurrence = reservationRepository.save(Reservation.builder()
                .memberId(member.getId()).reservationGroupId(group.getId())
                .reservationDate(pastDate).startMinute((short) 600).endMinute((short) 660)
                .purpose("반복").headcount((short) 2).build());
        Reservation futureOccurrence1 = reservationRepository.save(Reservation.builder()
                .memberId(member.getId()).reservationGroupId(group.getId())
                .reservationDate(futureDate1).startMinute((short) 600).endMinute((short) 660)
                .purpose("반복").headcount((short) 2).build());
        Reservation futureOccurrence2 = reservationRepository.save(Reservation.builder()
                .memberId(member.getId()).reservationGroupId(group.getId())
                .reservationDate(futureDate2).startMinute((short) 600).endMinute((short) 660)
                .purpose("반복").headcount((short) 2).build());

        mockMvc.perform(delete("/v1/reservations/groups/{groupId}", group.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(reservationRepository.findById(pastOccurrence.getId()).orElseThrow().isCancelled()).isFalse();
        assertThat(reservationRepository.findById(futureOccurrence1.getId()).orElseThrow().isCancelled()).isTrue();
        assertThat(reservationRepository.findById(futureOccurrence2.getId()).orElseThrow().isCancelled()).isTrue();
    }

    @Test
    void 존재하지_않는_그룹_취소시_404가_반환된다() throws Exception {
        String token = createActiveMemberAndLogin("20250607");

        mockMvc.perform(delete("/v1/reservations/groups/{groupId}", 999_999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void 타인의_그룹을_취소하면_403이_반환된다() throws Exception {
        Member owner = createActiveMember("20250608");
        String otherToken = createActiveMemberAndLogin("20250609");
        ReservationGroup group = reservationGroupRepository.save(ReservationGroup.builder()
                .memberId(owner.getId())
                .frequency(RepeatFrequency.WEEKLY)
                .weekdays(List.of(DayOfWeek.MONDAY))
                .startMinute((short) 600)
                .endMinute((short) 660)
                .repeatEndDate(LocalDate.now().plusWeeks(2))
                .createdAt(java.time.Instant.now())
                .build());

        mockMvc.perform(delete("/v1/reservations/groups/{groupId}", group.getId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
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
}
