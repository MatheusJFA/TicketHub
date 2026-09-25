package com.tickethub.infrastructure.authentication;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

@IntegrationTest
@DisplayName("Redis revoked access token gateway")
class RedisRevokedAccessTokenGatewayIT extends ContainerSupport {

    @Autowired
    private RedisRevokedAccessTokenGateway gateway;

    @Autowired
    private StringRedisTemplate redis;

    @Test
    @DisplayName("Given token id, when revoke, then is revoked until expiry")
    void givenTokenId_whenRevoke_thenIsRevokedUntilExpiry() {
        final var tokenId = "jti-" + UUID.randomUUID();

        assertFalse(gateway.isRevoked(tokenId));

        gateway.revoke(tokenId, Instant.now().plus(Duration.ofMinutes(30)));

        assertTrue(gateway.isRevoked(tokenId));
        assertTrue(Boolean.TRUE.equals(redis.hasKey("tickethub:auth:denylist:" + tokenId)));
    }

    @Test
    @DisplayName("Given expired token, when revoke, then stays not revoked")
    void givenExpiredToken_whenRevoke_thenStaysNotRevoked() {
        final var tokenId = "jti-" + UUID.randomUUID();

        gateway.revoke(tokenId, Instant.now().minus(Duration.ofMinutes(1)));

        assertFalse(gateway.isRevoked(tokenId));
    }
}
