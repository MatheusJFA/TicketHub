package com.tickethub.application.show.unpublishall;

import com.tickethub.domain.core.show.Show;

public record UnpublishAllShowOutput(String id) {
    public static UnpublishAllShowOutput from(final String id) {
        return new UnpublishAllShowOutput(id);
    }

    public static UnpublishAllShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
