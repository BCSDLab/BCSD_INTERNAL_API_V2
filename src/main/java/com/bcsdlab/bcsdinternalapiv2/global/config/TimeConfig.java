package com.bcsdlab.bcsdinternalapiv2.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 서비스의 "오늘"과 "지금"(회비 학기, 예약 가능 시각 등)은 한국 시간으로 정한다. 운영 컨테이너의
 * 시간대(UTC일 수 있음)에 기대지 않으려고 시스템 기본 시간대 대신 Asia/Seoul 고정 Clock을 주입한다.
 * 테스트는 이 빈을 바꿔 시각을 고정한다.
 */
@Configuration
public class TimeConfig {

    public static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(SEOUL);
    }
}
