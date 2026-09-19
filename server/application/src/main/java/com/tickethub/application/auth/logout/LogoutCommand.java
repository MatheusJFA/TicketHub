package com.tickethub.application.auth.logout;

public record LogoutCommand(String refreshToken) {
    public static LogoutCommand with(final String refreshToken) {
        return new LogoutCommand(refreshToken);
    }
}
