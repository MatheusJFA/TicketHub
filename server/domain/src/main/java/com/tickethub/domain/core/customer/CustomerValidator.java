package com.tickethub.domain.core.customer;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class CustomerValidator extends Validator {
    private final Customer customer;

    public CustomerValidator(final Customer customer, final ValidationHandler handler) {
        super(handler);
        this.customer = customer;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(customer.getCpf())) handler.append(new Error("'cpf' should not be null"));
        if (isNull(customer.getName())) handler.append(new Error("'name' should not be null"));
        if (isNull(customer.getEmail())) handler.append(new Error("'email' should not be null"));
        if (isNull(customer.getPasswordHash())) handler.append(new Error("'passwordHash' should not be null"));
    }
}
