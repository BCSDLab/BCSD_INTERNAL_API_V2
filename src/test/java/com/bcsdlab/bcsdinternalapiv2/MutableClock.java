package com.bcsdlab.bcsdinternalapiv2;

import com.bcsdlab.bcsdinternalapiv2.global.config.TimeConfig;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** 테스트에서 "오늘"을 바꾸는 Clock. 시간대는 운영과 같은 Asia/Seoul이다. */
public class MutableClock extends Clock {

    private volatile Instant instant = Instant.now();

    public void setToday(LocalDate date) {
        this.instant = date.atTime(12, 0).atZone(TimeConfig.SEOUL).toInstant();
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
        return TimeConfig.SEOUL;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return Clock.fixed(instant, zone);
    }

    @Override
    public Instant instant() {
        return instant;
    }
}
