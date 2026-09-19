package com.tickethub.domain.core.partner;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class PartnerValidator extends Validator {
    private final Partner partner;
    public PartnerValidator(final Partner partner, final ValidationHandler handler) { super(handler); this.partner = partner; }
    @Override public void validate() {
        final ValidationHandler handler = validationHandler();

        if (partner.getAddress() == null) handler.append(new Error("'address' should not be null"));
        if (partner.getName() == null) handler.append(new Error("'name' should not be null"));
        if (partner.getCnpj() == null) handler.append(new Error("'cnpj' should not be null"));
        if (partner.getEmail() == null) handler.append(new Error("'email' should not be null"));
        if (partner.getPasswordHash() == null) handler.append(new Error("'passwordHash' should not be null"));
    }
}
