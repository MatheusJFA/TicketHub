package com.tickethub.infrastructure.show.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.shared.Name;
import com.tickethub.domain.shared.Text;
import com.tickethub.infrastructure.shared.persistence.AddressDocument;

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

    public static ShowDocument from(final Show show) {
        return new ShowDocument(
                show.getId().getValue(),
                show.getName().getValue(),
                show.getDescription() == null ? null : show.getDescription().getValue(),
                show.getDate() == null ? null : show.getDate().toString(),
                AddressDocument.from(show.getAddress()),
                show.isPublished(),
                show.getTotalSpots(),
                show.getTotalSpotsSold(),
                show.getPartnerId() == null ? null : show.getPartnerId().getValue(),
                show.getSections().stream().map(section -> section.getId().getValue()).toList(),
                show.getCreatedAt(),
                show.getUpdatedAt(),
                show.getDeletedAt(),
                show.getCreatedBy(),
                show.getLastModifiedBy());
    }

    public Show toDomain(final Set<Section> sections) {
        return Show.reconstitute(
                ShowID.from(id),
                Name.create(name),
                Text.create(description),
                date == null ? null : OffsetDateTime.parse(date),
                address == null ? null : address.toDomain(),
                published,
                totalSpots,
                totalSpotsSold,
                partnerId == null ? null : PartnerID.from(partnerId),
                sections,
                createdAt, updatedAt, deletedAt, createdBy, lastModifiedBy);
    }
}
