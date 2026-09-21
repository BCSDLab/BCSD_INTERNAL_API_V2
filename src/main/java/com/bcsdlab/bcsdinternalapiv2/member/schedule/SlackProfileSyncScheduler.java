package com.bcsdlab.bcsdinternalapiv2.member.schedule;

import com.bcsdlab.bcsdinternalapiv2.member.controller.dto.response.SlackProfileSyncResponse;
import com.bcsdlab.bcsdinternalapiv2.member.service.SlackProfileSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlackProfileSyncScheduler {

    private final SlackProfileSyncService slackProfileSyncService;

    // 매주 일요일 00:05(KST)에 활동 회원의 프로필 사진을 Slack과 동기화한다.
    @Scheduled(cron = "0 5 0 * * SUN", zone = "Asia/Seoul")
    public void syncProfileImages() {
        SlackProfileSyncResponse result = slackProfileSyncService.syncAll();
        log.info("SLACK_PROFILE_SYNC_DONE: total={} updated={} failed={}",
                result.total(), result.updated(), result.failed());
    }
}
