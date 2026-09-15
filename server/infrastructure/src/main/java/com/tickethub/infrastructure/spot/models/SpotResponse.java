package com.tickethub.infrastructure.spot.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;
import com.tickethub.application.spot.retrieve.get.GetSpotOutput;

public record SpotResponse(String id, String location, boolean available, boolean published, Instant createdAt, Instant updatedAt, Instant deletedAt) {
    public static SpotResponse from(GetSpotOutput output) {
        return new SpotResponse(output.id(), output.location() == null ? null : output.location().getValue(), output.available(), output.published(), output.createdAt(), output.updatedAt(), output.deletedAt());
    }
}
