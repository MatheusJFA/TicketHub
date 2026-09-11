package com.tickethub.domain.core.partner;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.shared.CNPJ;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.validation.ValidationHandler;

public class Partner extends AggregateRoot<PartnerID> implements Cloneable {
    private Name name;
    private CNPJ cnpj;

    private Partner(PartnerID id, Name name, CNPJ cnpj) {
        super(id);
        this.name = name;
        this.cnpj = cnpj;
    }

    public static Partner create(String name, String cnpj) {
        final PartnerID id = PartnerID.generate();
        return new Partner(id, Name.create(name), CNPJ.create(cnpj));
    }

    public Name getName() {
        return name;
    }

    public CNPJ getCnpj() {
        return cnpj;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new PartnerValidator(this, handler);
        validator.validate();
    }

    @Override
    public Partner clone() throws CloneNotSupportedException {
        return (Partner) super.clone();
    }
}
