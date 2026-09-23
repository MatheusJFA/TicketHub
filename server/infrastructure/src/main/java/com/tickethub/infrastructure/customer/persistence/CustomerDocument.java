package com.tickethub.infrastructure.customer.persistence;

import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.shared.CPF;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
import com.tickethub.infrastructure.audit.AuditActor;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public CustomerDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new CustomerDocument(
                id, cpf, name, email, passwordHash, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public static CustomerDocument from(final Customer customer) {
        return stamped(new CustomerDocument(
                customer.getId().getValue(),
                customer.getCpf().getValue(),
                customer.getName().getValue(),
                Optional.ofNullable(customer.getEmail()).map(Email::getValue).orElse(null),
                Optional.ofNullable(customer.getPasswordHash())
                        .map(PasswordHash::getValue)
                        .orElse(null),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getDeletedAt(),
                customer.getCreatedBy(),
                customer.getLastModifiedBy()));
    }

    private static CustomerDocument stamped(final CustomerDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Customer toDomain() {
        // Legacy documents may predate email/passwordHash; nulls fail closed on
        // validation/login instead of breaking reads.
        final Email email = Optional.ofNullable(this.email).map(Email::create).orElse(null);
        final PasswordHash passwordHash = Optional.ofNullable(this.passwordHash)
                .map(PasswordHash::fromHash)
                .orElse(null);
        return Customer.reconstitute(
                CustomerID.from(id),
                CPF.create(cpf),
                Name.create(name),
                email,
                passwordHash,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }
}
