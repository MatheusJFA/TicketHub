package com.tickethub.application.customer.update;

import com.tickethub.domain.core.customer.Customer;

public record UpdateCustomerOutput(String id) {
    public static UpdateCustomerOutput from(final String id) {
        return new UpdateCustomerOutput(id);
    }

    public static UpdateCustomerOutput from(final Customer entity) {
        return from(entity.getId().getValue());
    }
}
