package com.tickethub.application.spot.retrieve.get;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Location;
import java.time.Instant;

public record GetSpotOutput(
        String id,
        Location location,
        boolean available,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
    public static GetSpotOutput from(final Spot entity) {
        return new GetSpotOutput(
                entity.getId().getValue(),
                entity.getLocation(),
                entity.isAvailable(),
                entity.isPublished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
