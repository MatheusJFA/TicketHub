package com.tickethub.infrastructure.section.models;

import com.tickethub.infrastructure.api.models.*;

public record CreateSectionRequest(String showId, String name, String description, long totalSpots, MoneyModel price) {}
