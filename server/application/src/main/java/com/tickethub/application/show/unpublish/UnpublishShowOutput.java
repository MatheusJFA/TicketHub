package com.tickethub.application.show.unpublish;

import com.tickethub.domain.core.show.Show;

public record UnpublishShowOutput(String id) {
    public static UnpublishShowOutput from(final String id) {
        return new UnpublishShowOutput(id);
    }

    public static UnpublishShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
