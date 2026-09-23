package com.tickethub.application.authentication.refresh;

public record RefreshTokenOutput(String accessToken, String tokenType, long expiresIn, String refreshToken) {}
