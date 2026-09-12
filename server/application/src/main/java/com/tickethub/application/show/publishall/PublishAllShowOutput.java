package com.tickethub.application.show.publishall;

import com.tickethub.domain.core.show.Show;

public record PublishAllShowOutput(String id) {
    public static PublishAllShowOutput from(final String id) {
        return new PublishAllShowOutput(id);
    }

    public static PublishAllShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
