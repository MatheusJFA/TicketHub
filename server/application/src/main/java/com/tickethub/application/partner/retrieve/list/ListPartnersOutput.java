package com.tickethub.application.partner.retrieve.list;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.shared.Address;
import java.time.Instant;

public record ListPartnersOutput(
        String id, String name, String cnpj, Address address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static ListPartnersOutput from(final Partner entity) {
        return new ListPartnersOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getCnpj().getValue(),
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
