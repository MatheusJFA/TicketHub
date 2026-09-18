package com.tickethub.infrastructure.section.models;

import java.time.Instant;
import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record SectionResponse(String id, String name, String description, MoneyModel price, boolean published, long totalSpots, long totalSpotsSold, Instant createdAt, Instant updatedAt, Instant deletedAt) {
}
