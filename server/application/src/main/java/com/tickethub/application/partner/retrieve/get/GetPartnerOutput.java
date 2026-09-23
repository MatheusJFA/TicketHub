package com.tickethub.application.partner.retrieve.get;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.shared.Address;
import java.time.Instant;

public record GetPartnerOutput(
        String id, String name, String cnpj, Address address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static GetPartnerOutput from(final Partner entity) {
        return new GetPartnerOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getCnpj().getValue(),
                entity.getAddress(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
