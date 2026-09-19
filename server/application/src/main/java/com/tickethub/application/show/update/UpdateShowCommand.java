package com.tickethub.application.show.update;

import java.time.OffsetDateTime;

public record UpdateShowCommand(String id, String name, String description, OffsetDateTime date) {
    public static UpdateShowCommand with(final String id, final String name, final String description,
            final OffsetDateTime date) {
        return new UpdateShowCommand(id, name, description, date);
    }
}
