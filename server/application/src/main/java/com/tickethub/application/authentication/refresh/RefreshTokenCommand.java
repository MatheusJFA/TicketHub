package com.tickethub.application.authentication.refresh;

public record RefreshTokenCommand(String refreshToken) {
    public static RefreshTokenCommand with(final String refreshToken) {
        return new RefreshTokenCommand(refreshToken);
    }
}
