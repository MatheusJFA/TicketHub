package com.tickethub.application.spot.update;

import com.tickethub.domain.core.spot.Spot;

public record UpdateSpotOutput(String id) {
    public static UpdateSpotOutput from(final String id) {
        return new UpdateSpotOutput(id);
    }

    public static UpdateSpotOutput from(final Spot entity) {
        return from(entity.getId().getValue());
    }
}
