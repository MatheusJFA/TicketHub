package com.tickethub.application.spot.update;

import com.tickethub.domain.shared.Location;

public record UpdateSpotCommand(String id, Location location) {
    public static UpdateSpotCommand with(final String id, final Location location) {
        return new UpdateSpotCommand(id, location);
    }
}
