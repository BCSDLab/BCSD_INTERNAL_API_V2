package com.bcsdlab.bcsdinternalapiv2.homepage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedEvent;
import com.bcsdlab.bcsdinternalapiv2.homepage.config.HomepageRevalidateProperties;
import com.bcsdlab.bcsdinternalapiv2.homepage.model.HomepageSyncStatus;
import com.bcsdlab.bcsdinternalapiv2.homepage.repository.HomepageSyncStatusRepository;
import com.bcsdlab.bcsdinternalapiv2.homepage.service.HomepageRevalidationListener;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * AC-5.4/AC-5.7: 트랜잭션 커밋 후 홈페이지 웹훅을 호출하고, 실패 시 1회 재시도한 뒤
 * 상태를 기록한다(ADR-010). Spring 컨텍스트 없이 JDK 내장 HttpServer로 홈페이지
 * revalidate 엔드포인트를 흉내 낸다 — 새 테스트 의존성을 추가하지 않는다.
 */
class HomepageRevalidationListenerTest {

    private HttpServer server;
    private BlockingQueue<String> receivedSecrets;
    private BlockingQueue<String> receivedBodies;
    private volatile int nextResponseStatus;

    @BeforeEach
    void setUp() throws IOException {
        receivedSecrets = new ArrayBlockingQueue<>(10);
        receivedBodies = new ArrayBlockingQueue<>(10);
        nextResponseStatus = 200;

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/api/revalidate", exchange -> {
            receivedSecrets.add(Optional.ofNullable(exchange.getRequestHeaders().getFirst("X-Revalidate-Secret"))
                    .orElse(""));
            receivedBodies.add(new String(exchange.getRequestBody().readAllBytes()));
            byte[] body = "{}".getBytes();
            exchange.sendResponseHeaders(nextResponseStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    @DisplayName("AC-5.4 커밋 후 이벤트를 받으면 시크릿 헤더와 태그를 담아 웹훅을 호출하고 성공 시각을 기록한다")
    void 웹훅_성공시_상태를_기록한다() throws InterruptedException {
        HomepageRevalidateProperties properties = new HomepageRevalidateProperties(url(), "test-secret");
        HomepageSyncStatusRepository repository = Mockito.mock(HomepageSyncStatusRepository.class);
        HomepageSyncStatus status = Mockito.mock(HomepageSyncStatus.class);
        when(repository.findById(1L)).thenReturn(Optional.of(status));

        HomepageRevalidationListener listener = new HomepageRevalidationListener(properties, repository);
        listener.onContentChanged(new ContentChangedEvent(List.of("track:backend", "track-list")));

        assertThat(receivedSecrets.poll(2, TimeUnit.SECONDS)).isEqualTo("test-secret");
        assertThat(receivedBodies.poll(2, TimeUnit.SECONDS)).contains("track:backend", "track-list");
        verify(status).markSucceeded(any());
        verify(status, never()).markFailed(any(), any());
    }

    @Test
    @DisplayName("AC-5.7 웹훅이 두 번 다 실패하면 실패 시각과 보류 태그를 기록한다(안전망 TTL이 나중에 따라잡는다)")
    void 웹훅_반복_실패시_실패_상태를_기록한다() throws InterruptedException {
        nextResponseStatus = 500;
        HomepageRevalidateProperties properties = new HomepageRevalidateProperties(url(), "test-secret");
        HomepageSyncStatusRepository repository = Mockito.mock(HomepageSyncStatusRepository.class);
        HomepageSyncStatus status = Mockito.mock(HomepageSyncStatus.class);
        when(repository.findById(1L)).thenReturn(Optional.of(status));

        HomepageRevalidationListener listener = new HomepageRevalidationListener(properties, repository);
        listener.onContentChanged(new ContentChangedEvent(List.of("activity:event")));

        // 1회 초기 시도 + 1회 재시도 = 총 2번 호출된다.
        assertThat(receivedSecrets.poll(2, TimeUnit.SECONDS)).isNotNull();
        assertThat(receivedSecrets.poll(2, TimeUnit.SECONDS)).isNotNull();
        verify(status).markFailed(any(), eq(List.of("activity:event")));
        verify(status, never()).markSucceeded(any());
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/api/revalidate";
    }
}
