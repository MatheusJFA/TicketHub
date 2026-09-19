package com.tickethub.infrastructure.spot.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Location;

@Document("spots")
public record SpotDocument(
        @Id String id,
        String location,
        boolean available,
        boolean published,
        String showId,
        String sectionId,
        String partnerId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "spots";

    public static SpotDocument from(final Spot spot) {
        return from(spot, null, null, null);
    }

    public static SpotDocument from(final Spot spot, final String showId, final String sectionId,
            final String partnerId) {
        return new SpotDocument(
                spot.getId().getValue(),
                spot.getLocation() == null ? null : spot.getLocation().getValue(),
                spot.isAvailable(),
                spot.isPublished(),
                showId,
                sectionId,
                partnerId,
                spot.getCreatedAt(),
                spot.getUpdatedAt(),
                spot.getDeletedAt(),
                spot.getCreatedBy(),
                spot.getLastModifiedBy());
    }

    public Spot toDomain() {
        return Spot.reconstitute(
                SpotID.from(id),
                location == null ? null : Location.create(location),
                available,
                published,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
