package com.tickethub.infrastructure.partner.models;

import com.tickethub.infrastructure.api.models.*;
import java.time.Instant;

public record PartnerListResponse(
        String id,
        String name,
        String cnpj,
        AddressModel address,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {}
