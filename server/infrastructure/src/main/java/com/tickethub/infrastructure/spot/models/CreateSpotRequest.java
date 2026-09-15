package com.tickethub.infrastructure.spot.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record CreateSpotRequest(String location) {}
