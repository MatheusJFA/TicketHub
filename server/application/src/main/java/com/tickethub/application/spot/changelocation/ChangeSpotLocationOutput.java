package com.tickethub.application.spot.changelocation;

import com.tickethub.domain.core.spot.Spot;

public record ChangeSpotLocationOutput(String id) {
    public static ChangeSpotLocationOutput from(final String id) {
        return new ChangeSpotLocationOutput(id);
    }

    public static ChangeSpotLocationOutput from(final Spot entity) {
        return from(entity.getId().getValue());
    }
}
