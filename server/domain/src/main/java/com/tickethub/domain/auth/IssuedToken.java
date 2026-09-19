package com.tickethub.domain.auth;

import org.apache.commons.lang3.StringUtils;

public record IssuedToken(String token, long expiresInSeconds) {

    public IssuedToken {
        if (StringUtils.isBlank(token)) {
            throw new IllegalArgumentException("'token' should not be null or blank");
        }
        if (expiresInSeconds <= 0) {
            throw new IllegalArgumentException("'expiresInSeconds' should be positive");
        }
    }
}
