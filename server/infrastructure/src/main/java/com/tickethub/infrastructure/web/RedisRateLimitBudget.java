package com.tickethub.infrastructure.web;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisRateLimitBudget implements RateLimitBudget {

    private static final Logger log = LoggerFactory.getLogger(RedisRateLimitBudget.class);
    private static final String KEY_PREFIX = "tickethub:ratelimit:";
    private static final long WINDOW_SECONDS = 60;
    private static final long KEY_TTL_SECONDS = 70;

    private static final RedisScript<Long> ACQUIRE_SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('INCR', KEYS[1]) "
                    + "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end "
                    + "return current",
            Long.class);

    private final StringRedisTemplate redis;

    public RedisRateLimitBudget(final StringRedisTemplate redis) {
        this.redis = requireNonNull(redis, "'redis' should not be null");
    }

    @Override
    public boolean tryAcquire(final String bucket, final int permitsPerMinute) {
        try {
            final long window = System.currentTimeMillis() / (WINDOW_SECONDS * 1000);
            final var key = KEY_PREFIX + bucket + ":" + window;
            final Long current = redis.execute(ACQUIRE_SCRIPT, List.of(key), String.valueOf(KEY_TTL_SECONDS));
            return current != null && current <= Math.max(1, permitsPerMinute);
        } catch (final RuntimeException e) {
            log.warn("Rate limit check failed (fail-open): {}", e.getMessage());
            return true;
        }
    }
}
