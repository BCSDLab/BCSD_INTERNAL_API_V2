package com.bcsdlab.bcsdinternalapiv2.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class ReservationConcurrencyTest extends ReservationIntegrationTestSupport {

    @Test
    void 동시에_같은_시간대를_예약하면_정확히_1건만_성공한다() throws Exception {
        String firstToken = createActiveMemberAndLogin("20250701");
        String secondToken = createActiveMemberAndLogin("20250702");
        LocalDate date = LocalDate.now().plusDays(3);
        String body = """
                {"date":"%s","start":600,"end":660,"purpose":"동시성테스트","headcount":2}
                """.formatted(date);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch readyLatch = new CountDownLatch(2);
        CountDownLatch startLatch = new CountDownLatch(1);

        List<Callable<Integer>> tasks = List.of(firstToken, secondToken).stream()
                .map(token -> (Callable<Integer>) () -> {
                    readyLatch.countDown();
                    startLatch.await();
                    MockHttpServletResponse response = mockMvc.perform(
                                    org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                            .post("/v1/reservations")
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(body))
                            .andReturn().getResponse();
                    return response.getStatus();
                })
                .collect(Collectors.toList());

        try {
            List<Future<Integer>> futures = tasks.stream().map(executor::submit).collect(Collectors.toList());
            readyLatch.await();
            startLatch.countDown();

            List<Integer> statuses = futures.stream().map(future -> {
                try {
                    return future.get();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).toList();

            long successCount = statuses.stream().filter(status -> status == 200).count();
            long conflictCount = statuses.stream().filter(status -> status == 409).count();

            assertThat(successCount).isEqualTo(1);
            assertThat(conflictCount).isEqualTo(1);
            assertThat(reservationRepository.findByReservationDateAndCancelledAtIsNullOrderByStartMinuteAsc(date))
                    .hasSize(1);
        } finally {
            executor.shutdown();
        }
    }
}
