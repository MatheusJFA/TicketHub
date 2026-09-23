package com.tickethub.infrastructure.show.models;

import com.tickethub.infrastructure.api.models.*;
import java.time.OffsetDateTime;

public record UpdateShowRequest(String name, String description, OffsetDateTime date) {}
