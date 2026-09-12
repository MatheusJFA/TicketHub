package com.tickethub.application.customer.create;

public record CreateCustomerCommand(String cpf, String name) {
    public static CreateCustomerCommand with(final String cpf, final String name) {
        return new CreateCustomerCommand(cpf, name);
    }
}
