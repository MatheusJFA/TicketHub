package com.tickethub.application.customer.create;

public record CreateCustomerCommand(String cpf, String name, String email, String password) {
    public static CreateCustomerCommand with(
            final String cpf, final String name, final String email, final String password) {
        return new CreateCustomerCommand(cpf, name, email, password);
    }
}
