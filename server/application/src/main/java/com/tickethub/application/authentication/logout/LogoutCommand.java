package com.tickethub.application.authentication.logout;

public record LogoutCommand(String refreshToken) {
    public static LogoutCommand with(final String refreshToken) {
        return new LogoutCommand(refreshToken);
    }
}
