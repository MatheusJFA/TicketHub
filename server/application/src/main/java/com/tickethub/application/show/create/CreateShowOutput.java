package com.tickethub.application.show.create;

import com.tickethub.domain.core.show.Show;

public record CreateShowOutput(String id) {
    public static CreateShowOutput from(final String id) {
        return new CreateShowOutput(id);
    }

    public static CreateShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
