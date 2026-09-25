package com.tickethub.infrastructure.web;

public interface RateLimitBudget {
    boolean tryAcquire(String bucket, int permitsPerMinute);
}
