package com.bcsdlab.bcsdinternalapiv2.ledger.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 회비의 "오늘"(현재 학기)은 한국 날짜로 정한다. 운영 컨테이너의 시간대(UTC일 수 있음)에 기대지 않으려고
 * 시스템 기본 시간대 대신 Asia/Seoul 고정 Clock을 주입한다. 테스트는 이 빈을 바꿔 날짜를 고정한다.
 */
@Configuration
public class LedgerTimeConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock ledgerClock() {
        return Clock.system(SEOUL);
    }
}
