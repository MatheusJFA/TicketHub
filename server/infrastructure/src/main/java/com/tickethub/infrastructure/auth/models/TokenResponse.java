package com.tickethub.infrastructure.auth.models;

public record TokenResponse(String token, String tokenType, long expiresIn) {

    public static TokenResponse bearer(final String token, final long expiresInSeconds) {
        return new TokenResponse(token, "Bearer", expiresInSeconds);
    }
}
