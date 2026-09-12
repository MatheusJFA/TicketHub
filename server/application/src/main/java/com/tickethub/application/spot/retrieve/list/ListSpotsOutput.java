package com.tickethub.application.spot.retrieve.list;

import java.time.Instant;
import com.tickethub.domain.shared.Location;
import com.tickethub.domain.core.spot.Spot;

public record ListSpotsOutput(
        String id,
        Location location,
        boolean available,
        boolean published,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
    public static ListSpotsOutput from(final Spot entity) {
        return new ListSpotsOutput(
                entity.getId().getValue(),
                entity.getLocation(),
                entity.isAvailable(),
                entity.isPublished(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
