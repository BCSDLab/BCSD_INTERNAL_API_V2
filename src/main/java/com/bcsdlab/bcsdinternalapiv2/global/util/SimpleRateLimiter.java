package com.bcsdlab.bcsdinternalapiv2.global.util;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

@Component
public class SimpleRateLimiter {

    private static final long SWEEP_INTERVAL_CALLS = 1000;

    private final ConcurrentHashMap<String, Instant> nextAllowedAt = new ConcurrentHashMap<>();
    private final AtomicLong callCount = new AtomicLong();

    public boolean tryAcquire(String key, Duration interval, Instant now) {
        boolean[] acquired = new boolean[1];
        nextAllowedAt.compute(key, (k, existing) -> {
            if (existing != null && now.isBefore(existing)) {
                acquired[0] = false;
                return existing;
            }
            acquired[0] = true;
            return now.plus(interval);
        });

        if (callCount.incrementAndGet() % SWEEP_INTERVAL_CALLS == 0) {
            nextAllowedAt.values().removeIf(expiresAt -> now.isAfter(expiresAt));
        }

        return acquired[0];
    }
}
