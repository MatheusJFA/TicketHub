package com.tickethub.domain.core.partner;

import java.time.Instant;
import java.time.OffsetDateTime;
import static java.util.Objects.isNull;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.CNPJ;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.validation.ValidationHandler;

public class Partner extends AggregateRoot<PartnerID> {
    private Name name;
    private final CNPJ cnpj;
    private Address address;
    private Email email;
    private PasswordHash passwordHash;

    private Partner(PartnerID id, Name name, CNPJ cnpj, Address address, Email email, PasswordHash passwordHash,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.name = name;
        this.cnpj = cnpj;
        this.address = requireAddress(address);
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static Partner create(String name, String cnpj, Address address, String email, String passwordHash) {
        final PartnerID id = PartnerID.generate();
        final var now = Instant.now();
        return new Partner(id, Name.create(name), CNPJ.create(cnpj), address, Email.create(email),
                PasswordHash.fromHash(passwordHash), now, now, null, null, null);
    }

    public static Partner reconstitute(PartnerID id, Name name, CNPJ cnpj, Address address, Email email,
            PasswordHash passwordHash,
            Instant createdAt, Instant updatedAt, Instant deletedAt, String createdBy, String lastModifiedBy) {
        return new Partner(id, name, cnpj, address, email, passwordHash, createdAt, updatedAt, deletedAt, createdBy,
                lastModifiedBy);
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

    public Partner changeEmail(final String email) {
        this.email = Email.create(email);
        markAsUpdated();
        return this;
    }

    public Partner changePassword(final String passwordHash) {
        this.passwordHash = PasswordHash.fromHash(passwordHash);
        markAsUpdated();
        return this;
    }

    private static Address requireAddress(final Address address) {
        if (isNull(address)) {
            throw new DomainException("'address' should not be null");
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

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new PartnerValidator(this, handler);
        validator.validate();
    }
}
