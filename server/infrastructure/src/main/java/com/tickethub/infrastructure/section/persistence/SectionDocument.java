package com.tickethub.infrastructure.section.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.infrastructure.shared.persistence.MoneyDocument;

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

    public static SectionDocument from(final Section section) {
        return from(section, null, null);
    }

    public static SectionDocument from(final Section section, final String showId, final String partnerId) {
        return new SectionDocument(
                section.getId().getValue(),
                section.getName().getValue(),
                section.getDescription() == null ? null : section.getDescription().getValue(),
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
                section.getLastModifiedBy());
    }

    public Section toDomain(final Set<Spot> spots) {
        return Section.reconstitute(
                SectionID.from(id),
                Name.create(name),
                Text.create(description),
                published,
                totalSpots,
                totalSpotsSold,
                price == null ? null : price.toDomain(),
                spots,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
