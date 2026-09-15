package com.tickethub.infrastructure.partner.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.partner.retrieve.list.ListPartnersOutput;

public record PartnerListResponse(String id, String name, String cnpj, AddressModel address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static PartnerListResponse from(ListPartnersOutput output) {
        return new PartnerListResponse(output.id(), output.name(), output.cnpj(), AddressModel.from(output.address()), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
