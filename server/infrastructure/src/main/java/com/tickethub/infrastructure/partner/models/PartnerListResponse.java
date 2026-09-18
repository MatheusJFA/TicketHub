package com.tickethub.infrastructure.partner.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record PartnerListResponse(String id, String name, String cnpj, AddressModel address, Instant createdAt, Instant updatedAt, Instant deletedAt) {
}
