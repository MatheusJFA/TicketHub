package com.tickethub.application.operator.create;

public record CreateOperatorCommand(String name, String email, String password) {
    public static CreateOperatorCommand with(final String name, final String email, final String password) {
        return new CreateOperatorCommand(name, email, password);
    }
}
