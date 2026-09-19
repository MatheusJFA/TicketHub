package com.tickethub.application.spot.create;

import com.tickethub.domain.shared.Location;

public record CreateSpotCommand(String sectionId, Location location) {
    public static CreateSpotCommand with(final String sectionId, final Location location) {
        return new CreateSpotCommand(sectionId, location);
    }
}
