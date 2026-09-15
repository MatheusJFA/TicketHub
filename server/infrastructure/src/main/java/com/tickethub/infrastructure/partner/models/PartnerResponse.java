package com.tickethub.infrastructure.partner.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.partner.retrieve.get.GetPartnerOutput;

public record PartnerResponse(String id, String name, String cnpj, AddressModel address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static PartnerResponse from(GetPartnerOutput output) {
        return new PartnerResponse(output.id(), output.name(), output.cnpj(), AddressModel.from(output.address()), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
