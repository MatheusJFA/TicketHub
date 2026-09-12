package com.tickethub.application.show.publish;

public record PublishShowCommand(String id) {
    public static PublishShowCommand with(final String id) {
        return new PublishShowCommand(id);
    }
}
