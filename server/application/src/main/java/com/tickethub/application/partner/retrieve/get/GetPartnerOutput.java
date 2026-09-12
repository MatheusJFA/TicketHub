package com.tickethub.application.partner.retrieve.get;
import java.time.Instant;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.core.partner.Partner;

public record GetPartnerOutput(String id, String name, String cnpj, Address address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static GetPartnerOutput from(final Partner entity) {
        return new GetPartnerOutput(entity.getId().getValue(), entity.getName().getValue(),
                entity.getCnpj().getValue(), entity.getAddress(), entity.getCreatedAt(), entity.getUpdatedAt(), entity.getDeletedAt());
    }
}
