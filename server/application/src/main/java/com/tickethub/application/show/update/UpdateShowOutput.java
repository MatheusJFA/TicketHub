package com.tickethub.application.show.update;

import com.tickethub.domain.core.show.Show;

public record UpdateShowOutput(String id) {
    public static UpdateShowOutput from(final String id) {
        return new UpdateShowOutput(id);
    }

    public static UpdateShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
