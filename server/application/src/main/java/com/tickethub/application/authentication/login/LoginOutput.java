package com.tickethub.application.authentication.login;

public record LoginOutput(String accessToken, String tokenType, long expiresIn, String refreshToken) {}
