package com.tickethub.infrastructure.web;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

class InMemoryRateLimitBudget implements RateLimitBudget {

    private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(final String bucket, final int permitsPerMinute) {
        return counters.computeIfAbsent(bucket, key -> new AtomicLong()).incrementAndGet()
                <= Math.max(1, permitsPerMinute);
    }
}
