package com.tickethub.infrastructure.show.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record ShowResponse(String id, String name, String description, OffsetDateTime date, AddressModel address, boolean published, long totalSpots, long totalSpotsSold, String partnerId, Instant createdAt, Instant updatedAt, Instant deletedAt) {
}
