package com.tickethub.application.spot.create;

import com.tickethub.domain.shared.Location;

public record CreateSpotCommand(Location location) {
    public static CreateSpotCommand with(final Location location) {
        return new CreateSpotCommand(location);
    }
}
