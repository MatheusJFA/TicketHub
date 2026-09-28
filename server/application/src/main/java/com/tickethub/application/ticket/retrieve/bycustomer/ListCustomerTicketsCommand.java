package com.tickethub.application.ticket.retrieve.bycustomer;

public record ListCustomerTicketsCommand(String customerId) {
    public static ListCustomerTicketsCommand with(final String customerId) {
        return new ListCustomerTicketsCommand(customerId);
    }
}
