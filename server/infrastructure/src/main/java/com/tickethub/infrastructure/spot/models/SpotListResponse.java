package com.tickethub.infrastructure.spot.models;

import java.time.Instant;

public record SpotListResponse(
        String id,
        String location,
        boolean available,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {}
