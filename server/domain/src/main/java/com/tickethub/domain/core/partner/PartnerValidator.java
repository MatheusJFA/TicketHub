package com.tickethub.domain.core.partner;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class PartnerValidator extends Validator {
    private final Partner partner;

    public PartnerValidator(final Partner partner, final ValidationHandler handler) {
        super(handler);
        this.partner = partner;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(partner.getAddress())) handler.append(new Error("'address' should not be null"));
        if (isNull(partner.getName())) handler.append(new Error("'name' should not be null"));
        if (isNull(partner.getCnpj())) handler.append(new Error("'cnpj' should not be null"));
        if (isNull(partner.getEmail())) handler.append(new Error("'email' should not be null"));
        if (isNull(partner.getPasswordHash())) handler.append(new Error("'passwordHash' should not be null"));
    }
}
