package com.tickethub.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayName("Redis rate limit budget")
class RedisRateLimitBudgetIT extends ContainerSupport {

    @Autowired
    private RedisRateLimitBudget budget;

    @Test
    @DisplayName("Given permits, when acquire within limit, then allows")
    void givenPermits_whenAcquireWithinLimit_thenAllows() {
        final var bucket = "test-" + UUID.randomUUID();

        assertTrue(budget.tryAcquire(bucket, 2));
        assertTrue(budget.tryAcquire(bucket, 2));
    }

    @Test
    @DisplayName("Given exhausted permits, when acquire, then denies")
    void givenExhaustedPermits_whenAcquire_thenDenies() {
        final var bucket = "test-" + UUID.randomUUID();

        assertTrue(budget.tryAcquire(bucket, 1));
        assertFalse(budget.tryAcquire(bucket, 1));
    }

    @Test
    @DisplayName("Given distinct buckets, when acquire, then counts independently")
    void givenDistinctBuckets_whenAcquire_thenCountsIndependently() {
        assertTrue(budget.tryAcquire("test-" + UUID.randomUUID(), 1));
        assertTrue(budget.tryAcquire("test-" + UUID.randomUUID(), 1));
    }
}
