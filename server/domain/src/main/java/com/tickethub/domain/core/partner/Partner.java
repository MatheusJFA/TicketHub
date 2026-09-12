package com.tickethub.domain.core.partner;

import java.time.OffsetDateTime;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.CNPJ;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.validation.ValidationHandler;

public class Partner extends AggregateRoot<PartnerID> implements Cloneable {
    private Name name;
    private final CNPJ cnpj;
    private Address address;

    private Partner(PartnerID id, Name name, CNPJ cnpj, Address address) {
        super(id);
        this.name = name;
        this.cnpj = cnpj;
        this.address = requireAddress(address);
    }

    public static Partner create(String name, String cnpj, Address address) {
        final PartnerID id = PartnerID.generate();
        return new Partner(id, Name.create(name), CNPJ.create(cnpj), address);
    }

    public Show createShow(String name, String description, OffsetDateTime date, Address address, long totalSpots) {
        return Show.create(name, description, date, address, totalSpots, this.getId());
    }

    public Partner changeName(final String name) {
        this.name = Name.create(name);
        markAsUpdated();
        return this;
    }

    public Partner changeAddress(final Address address) {
        this.address = requireAddress(address);
        markAsUpdated();
        return this;
    }

    private static Address requireAddress(final Address address) {
        if (address == null) {
            throw new com.tickethub.domain.exception.DomainException("'address' should not be null");
        }
        return address;
    }

    public Address getAddress() {
        return address;
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
