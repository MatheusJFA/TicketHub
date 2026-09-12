package com.tickethub.application.spot.unpublish;

import com.tickethub.domain.core.spot.Spot;

public record UnpublishSpotOutput(String id) {
    public static UnpublishSpotOutput from(final String id) {
        return new UnpublishSpotOutput(id);
    }

    public static UnpublishSpotOutput from(final Spot entity) {
        return from(entity.getId().getValue());
    }
}
