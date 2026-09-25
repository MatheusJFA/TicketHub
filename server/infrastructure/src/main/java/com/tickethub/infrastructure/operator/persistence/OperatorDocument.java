package com.tickethub.infrastructure.operator.persistence;

import com.tickethub.domain.core.operator.Operator;
import com.tickethub.domain.core.operator.OperatorID;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
import com.tickethub.infrastructure.audit.AuditActor;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("operators")
public record OperatorDocument(
        @Id String id,
        String name,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "operators";

    public OperatorDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new OperatorDocument(
                id, name, email, passwordHash, createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public static OperatorDocument from(final Operator operator) {
        return stamped(new OperatorDocument(
                operator.getId().getValue(),
                operator.getName().getValue(),
                Optional.ofNullable(operator.getEmail()).map(Email::getValue).orElse(null),
                Optional.ofNullable(operator.getPasswordHash())
                        .map(PasswordHash::getValue)
                        .orElse(null),
                operator.getCreatedAt(),
                operator.getUpdatedAt(),
                operator.getDeletedAt(),
                operator.getCreatedBy(),
                operator.getLastModifiedBy()));
    }

    private static OperatorDocument stamped(final OperatorDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Operator toDomain() {
        final Email email = Optional.ofNullable(this.email).map(Email::create).orElse(null);
        final PasswordHash passwordHash = Optional.ofNullable(this.passwordHash)
                .map(PasswordHash::fromHash)
                .orElse(null);
        return Operator.reconstitute(
                OperatorID.from(id),
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
