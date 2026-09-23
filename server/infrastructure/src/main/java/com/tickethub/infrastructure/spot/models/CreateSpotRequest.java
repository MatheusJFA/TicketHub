package com.tickethub.infrastructure.spot.models;

import com.tickethub.infrastructure.api.models.*;

public record CreateSpotRequest(String sectionId, String location) {}
