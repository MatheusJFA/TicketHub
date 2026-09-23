package com.tickethub.application.spot.retrieve.list;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Location;
import java.time.Instant;

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
