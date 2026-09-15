package com.tickethub.infrastructure.spot.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.spot.retrieve.list.ListSpotsOutput;

public record SpotListResponse(String id, String location, boolean available, boolean published, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static SpotListResponse from(ListSpotsOutput output) {
        return new SpotListResponse(output.id(), output.location() == null ? null : output.location().getValue(), output.available(), output.published(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
