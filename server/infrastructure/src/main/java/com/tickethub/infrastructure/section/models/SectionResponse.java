package com.tickethub.infrastructure.section.models;

import com.tickethub.infrastructure.api.models.*;
import java.time.Instant;

public record SectionResponse(
        String id,
        String name,
        String description,
        MoneyModel price,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {}
