package com.tickethub.infrastructure.section.persistence;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.shared.persistence.MoneyDocument;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("sections")
public record SectionDocument(
        @Id String id,
        String name,
        String description,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        MoneyDocument price,
        List<String> spotIds,
        String showId,
        String partnerId,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "sections";

    public SectionDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new SectionDocument(
                id,
                name,
                description,
                published,
                totalSpots,
                totalSpotsSold,
                price,
                spotIds,
                showId,
                partnerId,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }

    public static SectionDocument from(final Section section) {
        return from(section, null, null);
    }

    public static SectionDocument from(final Section section, final String showId, final String partnerId) {
        return stamped(new SectionDocument(
                section.getId().getValue(),
                section.getName().getValue(),
                Optional.ofNullable(section.getDescription())
                        .map(Text::getValue)
                        .orElse(null),
                section.isPublished(),
                section.getTotalSpots(),
                section.getTotalSpotsSold(),
                MoneyDocument.from(section.getPrice()),
                section.getSpots().stream().map(spot -> spot.getId().getValue()).toList(),
                showId,
                partnerId,
                section.getCreatedAt(),
                section.getUpdatedAt(),
                section.getDeletedAt(),
                section.getCreatedBy(),
                section.getLastModifiedBy()));
    }

    private static SectionDocument stamped(final SectionDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Section toDomain(final Set<Spot> spots) {
        return Section.reconstitute(
                SectionID.from(id),
                Name.create(name),
                Text.create(description),
                published,
                totalSpots,
                totalSpotsSold,
                Optional.ofNullable(price).map(MoneyDocument::toDomain).orElse(null),
                spots,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }
}
