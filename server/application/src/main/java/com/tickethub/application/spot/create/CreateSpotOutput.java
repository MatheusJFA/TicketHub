package com.tickethub.application.spot.create;

import com.tickethub.domain.core.spot.Spot;

public record CreateSpotOutput(String id) {
    public static CreateSpotOutput from(final String id) {
        return new CreateSpotOutput(id);
    }

    public static CreateSpotOutput from(final Spot entity) {
        return from(entity.getId().getValue());
    }
}
