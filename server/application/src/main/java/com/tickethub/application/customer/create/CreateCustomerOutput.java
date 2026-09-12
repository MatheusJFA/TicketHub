package com.tickethub.application.customer.create;

import com.tickethub.domain.core.customer.Customer;

public record CreateCustomerOutput(String id) {
    public static CreateCustomerOutput from(final String id) {
        return new CreateCustomerOutput(id);
    }

    public static CreateCustomerOutput from(final Customer entity) {
        return from(entity.getId().getValue());
    }
}
