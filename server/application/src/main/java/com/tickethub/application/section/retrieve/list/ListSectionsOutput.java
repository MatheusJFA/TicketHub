package com.tickethub.application.section.retrieve.list;

import java.time.Instant;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.core.section.Section;

public record ListSectionsOutput(
        String id,
        String name,
        String description,
        Money price,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt) {
    public static ListSectionsOutput from(final Section entity) {
        return new ListSectionsOutput(
                entity.getId().getValue(),
                entity.getName().getValue(),
                entity.getDescription().getValue(),
                entity.getPrice(),
                entity.isPublished(),
                entity.getTotalSpots(),
                entity.getTotalSpotsSold(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt());
    }
}
