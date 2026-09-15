package com.tickethub.infrastructure.section.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.section.retrieve.list.ListSectionsOutput;

public record SectionListResponse(String id, String name, String description, MoneyModel price, boolean published, long totalSpots, long totalSpotsSold, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static SectionListResponse from(ListSectionsOutput output) {
        return new SectionListResponse(output.id(), output.name(), output.description(), MoneyModel.from(output.price()), output.published(), output.totalSpots(), output.totalSpotsSold(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
