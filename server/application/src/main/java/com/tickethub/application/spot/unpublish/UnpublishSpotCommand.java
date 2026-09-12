package com.tickethub.application.spot.unpublish;

public record UnpublishSpotCommand(String id) {
    public static UnpublishSpotCommand with(final String id) {
        return new UnpublishSpotCommand(id);
    }
}
