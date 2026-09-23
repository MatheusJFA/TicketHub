package com.tickethub.application.customer.changename;

public record ChangeCustomerNameCommand(String id, String name) {
    public static ChangeCustomerNameCommand with(final String id, final String name) {
        return new ChangeCustomerNameCommand(id, name);
    }
}
