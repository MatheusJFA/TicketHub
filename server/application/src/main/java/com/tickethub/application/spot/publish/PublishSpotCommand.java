package com.tickethub.application.spot.publish;

public record PublishSpotCommand(String id) {
    public static PublishSpotCommand with(final String id) {
        return new PublishSpotCommand(id);
    }
}
