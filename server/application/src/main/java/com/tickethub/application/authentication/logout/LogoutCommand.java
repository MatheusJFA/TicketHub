package com.tickethub.application.authentication.logout;

public record LogoutCommand(String refreshToken, String accessToken) {
    public static LogoutCommand with(final String refreshToken) {
        return new LogoutCommand(refreshToken, null);
    }

    public static LogoutCommand with(final String refreshToken, final String accessToken) {
        return new LogoutCommand(refreshToken, accessToken);
    }
}
