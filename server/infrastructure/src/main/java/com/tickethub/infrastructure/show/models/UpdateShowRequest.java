package com.tickethub.infrastructure.show.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record UpdateShowRequest(String name, String description, OffsetDateTime date) {}
