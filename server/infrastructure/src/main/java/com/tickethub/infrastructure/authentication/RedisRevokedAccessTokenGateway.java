package com.tickethub.infrastructure.authentication;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisRevokedAccessTokenGateway implements RevokedAccessTokenGateway {

    private static final Logger log = LoggerFactory.getLogger(RedisRevokedAccessTokenGateway.class);
    private static final String KEY_PREFIX = "tickethub:auth:denylist:";

    private final StringRedisTemplate redis;

    public RedisRevokedAccessTokenGateway(final StringRedisTemplate redis) {
        this.redis = requireNonNull(redis, "'redis' should not be null");
    }

    @Override
    public void revoke(final String tokenId, final Instant expiresAt) {
        try {
            final var ttl = Duration.between(Instant.now(), expiresAt);
            if (ttl.isNegative() || ttl.isZero()) {
                return;
            }
            redis.opsForValue().set(key(tokenId), "1", ttl);
        } catch (final RuntimeException e) {
            log.warn("Revoking access token failed (fail-open): {}", e.getMessage());
        }
    }

    @Override
    public boolean isRevoked(final String tokenId) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(key(tokenId)));
        } catch (final RuntimeException e) {
            log.warn("Checking revoked access token failed (fail-open): {}", e.getMessage());
            return false;
        }
    }

    private static String key(final String tokenId) {
        return KEY_PREFIX + tokenId;
    }
}
