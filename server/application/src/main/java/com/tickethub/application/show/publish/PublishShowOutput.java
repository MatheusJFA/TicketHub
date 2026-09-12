package com.tickethub.application.show.publish;

import com.tickethub.domain.core.show.Show;

public record PublishShowOutput(String id) {
    public static PublishShowOutput from(final String id) {
        return new PublishShowOutput(id);
    }

    public static PublishShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
