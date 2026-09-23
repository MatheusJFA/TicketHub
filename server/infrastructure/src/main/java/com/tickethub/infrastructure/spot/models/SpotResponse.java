package com.tickethub.infrastructure.spot.models;

import java.time.Instant;

public record SpotResponse(
        String id,
        String location,
        boolean available,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {}
