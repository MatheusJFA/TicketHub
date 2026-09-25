package com.tickethub.domain.core.operator;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class OperatorValidator extends Validator {
    private final Operator operator;

    public OperatorValidator(final Operator operator, final ValidationHandler handler) {
        super(handler);
        this.operator = operator;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(operator.getName())) handler.append(new Error("'name' should not be null"));
        if (isNull(operator.getEmail())) handler.append(new Error("'email' should not be null"));
        if (isNull(operator.getPasswordHash())) handler.append(new Error("'passwordHash' should not be null"));
    }
}
