package com.tickethub.infrastructure.show.models;

import com.tickethub.infrastructure.api.models.*;

public record AddSectionToShowRequest(String name, String description, long totalSpots, MoneyModel price) {}
