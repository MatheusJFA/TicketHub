package com.tickethub.application.auth.refresh;

public record RefreshTokenOutput(String accessToken, String tokenType, long expiresIn, String refreshToken) {
}
