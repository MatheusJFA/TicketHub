package com.tickethub.infrastructure.section.models;

import com.tickethub.infrastructure.api.models.*;

public record UpdateSectionRequest(String name, String description, MoneyModel price) {}
