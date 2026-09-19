package com.tickethub.infrastructure.section.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record UpdateSectionRequest(String name, String description, MoneyModel price) {}
