package com.tickethub.application.show.publishall;

public record PublishAllShowCommand(String id) {
    public static PublishAllShowCommand with(final String id) {
        return new PublishAllShowCommand(id);
    }
}
