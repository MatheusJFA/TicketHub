package com.tickethub.application.auth.login;

public record LoginOutput(String accessToken, String tokenType, long expiresIn, String refreshToken) {
}
