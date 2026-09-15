package com.tickethub.infrastructure.show.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.show.retrieve.list.ListShowsOutput;

public record ShowListResponse(String id, String name, String description, OffsetDateTime date, AddressModel address, boolean published, long totalSpots, long totalSpotsSold, String partnerId, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static ShowListResponse from(ListShowsOutput output) {
        return new ShowListResponse(output.id(), output.name(), output.description(), output.date(), AddressModel.from(output.address()), output.published(), output.totalSpots(), output.totalSpotsSold(), output.partnerId(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
