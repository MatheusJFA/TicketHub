package com.tickethub.domain.auth;

import static org.apache.commons.lang3.StringUtils.isBlank;

public record IssuedToken(String token, long expiresInSeconds) {

    public IssuedToken {
        if (isBlank(token)) {
            throw new IllegalArgumentException("'token' should not be null or blank");
        }
        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("'expiresInSeconds' should be positive");
        }
    }
}
