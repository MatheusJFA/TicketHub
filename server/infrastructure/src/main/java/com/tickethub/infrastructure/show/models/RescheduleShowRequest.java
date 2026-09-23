package com.tickethub.infrastructure.show.models;

import com.tickethub.infrastructure.api.models.*;
import java.time.OffsetDateTime;

public record RescheduleShowRequest(OffsetDateTime date) {}
