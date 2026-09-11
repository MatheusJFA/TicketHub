package com.tickethub.domain.core.customer;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class CustomerValidator extends Validator {
    private final Customer customer;
    public CustomerValidator(final Customer customer, final ValidationHandler handler) { super(handler); this.customer = customer; }
    @Override public void validate() {
        if (customer.getCpf() == null) validationHandler().append(new Error("'cpf' should not be null"));
        if (customer.getName() == null) validationHandler().append(new Error("'name' should not be null"));
    }
}
