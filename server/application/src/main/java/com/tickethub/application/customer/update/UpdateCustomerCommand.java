package com.tickethub.application.customer.update;

public record UpdateCustomerCommand(String id, String name) {
    public static UpdateCustomerCommand with(final String id, final String name) {
        return new UpdateCustomerCommand(id, name);
    }
}
