package com.tickethub.domain.authentication;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.time.Instant;
import java.util.Objects;

public record AccessTokenIdentity(String tokenId, Instant expiresAt) {

    public AccessTokenIdentity {
        if (isBlank(tokenId)) {
            throw new IllegalArgumentException("'tokenId' should not be null or blank");
        }
        Objects.requireNonNull(expiresAt, "'expiresAt' should not be null");
    }
}
