package com.tickethub.application.spot.publish;

import com.tickethub.domain.core.spot.Spot;

public record PublishSpotOutput(String id) {
    public static PublishSpotOutput from(final String id) {
        return new PublishSpotOutput(id);
    }

    public static PublishSpotOutput from(final Spot entity) {
        return from(entity.getId().getValue());
    }
}
