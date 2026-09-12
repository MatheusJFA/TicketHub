package com.tickethub.application.partner.retrieve.list;
import java.time.Instant;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.core.partner.Partner;

public record ListPartnersOutput(String id, String name, String cnpj, Address address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static ListPartnersOutput from(final Partner entity) {
        return new ListPartnersOutput(entity.getId().getValue(), entity.getName().getValue(),
                entity.getCnpj().getValue(), entity.getAddress(), entity.getCreatedAt(), entity.getUpdatedAt(), entity.getDeletedAt());
    }
}
