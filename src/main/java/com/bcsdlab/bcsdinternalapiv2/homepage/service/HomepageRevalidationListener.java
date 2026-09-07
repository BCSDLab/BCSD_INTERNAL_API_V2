package com.bcsdlab.bcsdinternalapiv2.homepage.service;

import com.bcsdlab.bcsdinternalapiv2.global.event.ContentChangedEvent;
import com.bcsdlab.bcsdinternalapiv2.homepage.config.HomepageRevalidateProperties;
import com.bcsdlab.bcsdinternalapiv2.homepage.model.HomepageSyncStatus;
import com.bcsdlab.bcsdinternalapiv2.homepage.repository.HomepageSyncStatusRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * 콘텐츠 변경 트랜잭션이 커밋된 뒤에만 홈페이지 웹훅을 호출한다(ADR-010). 실패해도
 * 저장을 롤백시키지 않는다 — best-effort, 1회 재시도 후 로그만 남기고 상태를 기록한다.
 * 안전망 TTL(revalidate: 3600)이 최악의 경우 1시간 안에 따라잡는다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HomepageRevalidationListener {

    private static final long STATUS_ID = 1L;

    private final HomepageRevalidateProperties properties;
    private final HomepageSyncStatusRepository statusRepository;
    private final RestClient restClient = RestClient.create();

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onContentChanged(ContentChangedEvent event) {
        boolean success = callRevalidate(event.tags()) || callRevalidate(event.tags());
        HomepageSyncStatus status = statusRepository.findById(STATUS_ID).orElseThrow();
        if (success) {
            status.markSucceeded(Instant.now());
        } else {
            status.markFailed(Instant.now(), event.tags());
        }
    }

    private boolean callRevalidate(List<String> tags) {
        try {
            restClient.post()
                    .uri(properties.url())
                    .header("X-Revalidate-Secret", properties.secret())
                    .body(new RevalidateRequest(tags))
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            log.warn("홈페이지 revalidate 웹훅 실패: tags={}", tags, e);
            return false;
        }
    }

    private record RevalidateRequest(List<String> tags) {
    }
}
