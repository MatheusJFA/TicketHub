package com.tickethub.infrastructure.show.models;

import com.tickethub.infrastructure.api.models.*;
import java.time.Instant;
import java.time.OffsetDateTime;

public record ShowListResponse(
        String id,
        String name,
        String description,
        OffsetDateTime date,
        AddressModel address,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        String partnerId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {}
