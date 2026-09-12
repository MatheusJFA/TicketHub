package com.tickethub.application.spot.changelocation;

import com.tickethub.domain.shared.Location;

public record ChangeSpotLocationCommand(String id, Location location) {
    public static ChangeSpotLocationCommand with(final String id, final Location location) {
        return new ChangeSpotLocationCommand(id, location);
    }
}
