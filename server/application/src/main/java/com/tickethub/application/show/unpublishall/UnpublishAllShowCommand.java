package com.tickethub.application.show.unpublishall;

public record UnpublishAllShowCommand(String id) {
    public static UnpublishAllShowCommand with(final String id) {
        return new UnpublishAllShowCommand(id);
    }
}
