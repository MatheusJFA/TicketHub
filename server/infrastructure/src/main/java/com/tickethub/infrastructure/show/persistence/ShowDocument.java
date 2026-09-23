package com.tickethub.infrastructure.show.persistence;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.infrastructure.audit.AuditActor;
import com.tickethub.infrastructure.shared.persistence.AddressDocument;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("shows")
public record ShowDocument(
        @Id String id,
        String name,
        String description,
        String date,
        AddressDocument address,
        boolean published,
        long totalSpots,
        long totalSpotsSold,
        String partnerId,
        List<String> sectionIds,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        String createdBy,
        String lastModifiedBy) {

    public static final String COLLECTION = "shows";

    public ShowDocument withActors(final String createdBy, final String lastModifiedBy) {
        return new ShowDocument(
                id,
                name,
                description,
                date,
                address,
                published,
                totalSpots,
                totalSpotsSold,
                partnerId,
                sectionIds,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }

    public static ShowDocument from(final Show show) {
        return stamped(new ShowDocument(
                show.getId().getValue(),
                show.getName().getValue(),
                Optional.ofNullable(show.getDescription()).map(Text::getValue).orElse(null),
                Optional.ofNullable(show.getDate())
                        .map(OffsetDateTime::toString)
                        .orElse(null),
                AddressDocument.from(show.getAddress()),
                show.isPublished(),
                show.getTotalSpots(),
                show.getTotalSpotsSold(),
                Optional.ofNullable(show.getPartnerId())
                        .map(PartnerID::getValue)
                        .orElse(null),
                show.getSections().stream()
                        .map(section -> section.getId().getValue())
                        .toList(),
                show.getCreatedAt(),
                show.getUpdatedAt(),
                show.getDeletedAt(),
                show.getCreatedBy(),
                show.getLastModifiedBy()));
    }

    private static ShowDocument stamped(final ShowDocument document) {
        final var actor = AuditActor.currentOrAnonymous();
        final var createdBy = Optional.ofNullable(document.createdBy()).orElse(actor);
        return document.withActors(createdBy, actor);
    }

    public Show toDomain(final Set<Section> sections) {
        return Show.reconstitute(
                ShowID.from(id),
                Name.create(name),
                Text.create(description),
                Optional.ofNullable(date).map(OffsetDateTime::parse).orElse(null),
                Optional.ofNullable(address).map(AddressDocument::toDomain).orElse(null),
                published,
                totalSpots,
                totalSpotsSold,
                Optional.ofNullable(partnerId).map(PartnerID::from).orElse(null),
                sections,
                createdAt,
                updatedAt,
                deletedAt,
                createdBy,
                lastModifiedBy);
    }
}
