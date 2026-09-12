package com.tickethub.application.show.unpublish;

public record UnpublishShowCommand(String id) {
    public static UnpublishShowCommand with(final String id) {
        return new UnpublishShowCommand(id);
    }
}
