package com.tickethub.application.show.retrieve.get;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.shared.Address;

public record GetShowOutput(
        String id,
        String name,
        String description,
        OffsetDateTime date,
        Address address,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        String partnerId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
    public static GetShowOutput from(final Show entity) {
        return new GetShowOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getDescription().getValue(),
                entity.getDate(),
                entity.getAddress(),
                entity.isPublished(),
                entity.getTotalSpots(),
                entity.getTotalSpotsSold(),
                entity.getPartnerId().getValue(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
