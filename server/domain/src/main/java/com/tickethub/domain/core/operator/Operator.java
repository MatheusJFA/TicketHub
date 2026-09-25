package com.tickethub.domain.core.operator;

import com.tickethub.domain.AggregateRoot;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
import com.tickethub.domain.validation.ValidationHandler;
import java.time.Instant;

public class Operator extends AggregateRoot<OperatorID> {
    private Name name;
    private Email email;
    private PasswordHash passwordHash;

    private Operator(
            OperatorID id,
            Name name,
            Email email,
            PasswordHash passwordHash,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        super(id, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public static Operator create(String name, String email, String passwordHash) {
        final OperatorID id = OperatorID.generate();
        final var now = Instant.now();
        return new Operator(
                id,
                Name.create(name),
                Email.create(email),
                PasswordHash.fromHash(passwordHash),
                now,
                now,
                null,
                null,
                null);
    }

    public static Operator reconstitute(
            OperatorID id,
            Name name,
            Email email,
            PasswordHash passwordHash,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            String createdBy,
            String lastModifiedBy) {
        return new Operator(id, name, email, passwordHash, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public Operator changeName(final String name) {
        this.name = Name.create(name);
        markAsUpdated();
        return this;
    }

    public Operator changeEmail(final String email) {
        this.email = Email.create(email);
        markAsUpdated();
        return this;
    }

    public Operator changePassword(final String passwordHash) {
        this.passwordHash = PasswordHash.fromHash(passwordHash);
        markAsUpdated();
        return this;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public PasswordHash getPasswordHash() {
        return passwordHash;
    }

    @Override
    public void validate(final ValidationHandler handler) {
        final var validator = new OperatorValidator(this, handler);
        validator.validate();
    }
}
