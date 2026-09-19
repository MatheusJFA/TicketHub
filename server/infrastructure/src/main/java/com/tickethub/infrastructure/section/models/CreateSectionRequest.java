package com.tickethub.infrastructure.section.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record CreateSectionRequest(String showId, String name, String description, long totalSpots, MoneyModel price) {}
