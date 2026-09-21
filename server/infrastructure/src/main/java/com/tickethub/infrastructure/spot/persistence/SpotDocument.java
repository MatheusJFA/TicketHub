package com.tickethub.infrastructure.spot.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.shared.Location;
import com.tickethub.infrastructure.audit.AuditActor;

@Document("spots")
public record SpotDocument(
        @Id String id,
        String location,
        boolean available,
        boolean published,
        boolean reserved,
        String showId,
        String sectionId,
        String partnerId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "spots";

    public SpotDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new SpotDocument(id, location, available, published, reserved, showId, sectionId, partnerId,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }

    public static SpotDocument from(final Spot spot) {
        return from(spot, null, null, null);
    }

    public static SpotDocument from(final Spot spot, final String showId, final String sectionId,
            final String partnerId) {
        return stamped(new SpotDocument(
                spot.getId().getValue(),
                Optional.ofNullable(spot.getLocation()).map(Location::getValue).orElse(null),
                spot.isAvailable(),
                spot.isPublished(),
                spot.isReserved(),
                showId,
                sectionId,
                partnerId,
                spot.getCreatedAt(),
                spot.getUpdatedAt(),
                spot.getDeletedAt(),
                spot.getCreatedBy(),
                spot.getLastModifiedBy()));
    }

    private static SpotDocument stamped(final SpotDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Spot toDomain() {
        return Spot.reconstitute(
                SpotID.from(id),
                Optional.ofNullable(location).map(Location::create).orElse(null),
                available,
                published,
                reserved,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
