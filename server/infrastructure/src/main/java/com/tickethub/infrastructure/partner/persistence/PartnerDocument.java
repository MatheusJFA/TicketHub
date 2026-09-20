package com.tickethub.infrastructure.partner.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.shared.CNPJ;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.shared.persistence.AddressDocument;

@Document("partners")
public record PartnerDocument(
        @Id String id,
        String name,
        String cnpj,
        AddressDocument address,
        String email,
        String passwordHash,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "partners";

    public PartnerDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new PartnerDocument(id, name, cnpj, address, email, passwordHash, createdAt,
                updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public static PartnerDocument from(final Partner partner) {
        return stamped(new PartnerDocument(
                partner.getId().getValue(),
                partner.getName().getValue(),
                partner.getCnpj().getValue(),
                AddressDocument.from(partner.getAddress()),
                Optional.ofNullable(partner.getEmail()).map(Email::getValue).orElse(null),
                Optional.ofNullable(partner.getPasswordHash()).map(PasswordHash::getValue).orElse(null),
                partner.getCreatedAt(),
                partner.getUpdatedAt(),
                partner.getDeletedAt(),
                partner.getCreatedBy(),
                partner.getLastModifiedBy()));
    }

    private static PartnerDocument stamped(final PartnerDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Partner toDomain() {
        // Legacy documents may predate email/passwordHash; nulls fail closed on
        // validation/login instead of breaking reads.
        final Email email = Optional.ofNullable(this.email).map(Email::create).orElse(null);
        final PasswordHash passwordHash = Optional.ofNullable(this.passwordHash)
                .map(PasswordHash::fromHash).orElse(null);
        return Partner.reconstitute(
                PartnerID.from(id),
                Name.create(name),
                CNPJ.create(cnpj),
                address.toDomain(),
                email,
                passwordHash,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
