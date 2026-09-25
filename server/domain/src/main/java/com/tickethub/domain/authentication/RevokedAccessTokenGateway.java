package com.tickethub.domain.authentication;

import java.time.Instant;

public interface RevokedAccessTokenGateway {
    void revoke(String tokenId, Instant expiresAt);

    boolean isRevoked(String tokenId);
}
