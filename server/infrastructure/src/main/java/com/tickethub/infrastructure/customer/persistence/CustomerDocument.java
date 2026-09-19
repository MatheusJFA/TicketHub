package com.tickethub.infrastructure.customer.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.shared.CPF;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;

@Document("customers")
public record CustomerDocument(
        @Id String id,
        String cpf,
        String name,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "customers";

    public static CustomerDocument from(final Customer customer) {
        return new CustomerDocument(
                customer.getId().getValue(),
                customer.getCpf().getValue(),
                customer.getName().getValue(),
                customer.getEmail() == null ? null : customer.getEmail().getValue(),
                customer.getPasswordHash() == null ? null : customer.getPasswordHash().getValue(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getDeletedAt(),
                customer.getCreatedBy(),
                customer.getLastModifiedBy());
    }

    public Customer toDomain() {
        // Legacy documents may predate email/passwordHash; nulls fail closed on
        // validation/login instead of breaking reads.
        final Email email = this.email == null ? null : Email.create(this.email);
        final PasswordHash passwordHash = this.passwordHash == null ? null : PasswordHash.fromHash(this.passwordHash);
        return Customer.reconstitute(
                CustomerID.from(id),
                CPF.create(cpf),
                Name.create(name),
                email,
                passwordHash,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
