package com.tickethub.domain.core.customer;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class CustomerValidator extends Validator {
    private final Customer customer;
    public CustomerValidator(final Customer customer, final ValidationHandler handler) { super(handler); this.customer = customer; }
    @Override public void validate() {
        final ValidationHandler handler = validationHandler();

        if (customer.getCpf() == null) handler.append(new Error("'cpf' should not be null"));
        if (customer.getName() == null) handler.append(new Error("'name' should not be null"));
        if (customer.getEmail() == null) handler.append(new Error("'email' should not be null"));
        if (customer.getPasswordHash() == null) handler.append(new Error("'passwordHash' should not be null"));
    }
}
