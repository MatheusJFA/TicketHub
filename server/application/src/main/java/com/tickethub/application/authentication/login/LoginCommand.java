package com.tickethub.application.authentication.login;

public record LoginCommand(String identifier, String password) {
    public static LoginCommand with(final String identifier, final String password) {
        return new LoginCommand(identifier, password);
    }
}
