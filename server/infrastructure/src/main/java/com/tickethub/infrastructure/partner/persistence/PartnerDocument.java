package com.tickethub.infrastructure.partner.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.shared.CNPJ;
import com.tickethub.domain.shared.Email;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.PasswordHash;
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

    public static PartnerDocument from(final Partner partner) {
        return new PartnerDocument(
                partner.getId().getValue(),
                partner.getName().getValue(),
                partner.getCnpj().getValue(),
                AddressDocument.from(partner.getAddress()),
                partner.getEmail() == null ? null : partner.getEmail().getValue(),
                partner.getPasswordHash() == null ? null : partner.getPasswordHash().getValue(),
                partner.getCreatedAt(),
                partner.getUpdatedAt(),
                partner.getDeletedAt(),
                partner.getCreatedBy(),
                partner.getLastModifiedBy());
    }

    public Partner toDomain() {
        // Legacy documents may predate email/passwordHash; nulls fail closed on
        // validation/login instead of breaking reads.
        final Email email = this.email == null ? null : Email.create(this.email);
        final PasswordHash passwordHash = this.passwordHash == null ? null : PasswordHash.fromHash(this.passwordHash);
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
