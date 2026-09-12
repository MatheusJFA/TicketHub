package com.tickethub.application.show.addsection;

import com.tickethub.domain.core.show.Show;

public record AddSectionToShowOutput(String id) {
    public static AddSectionToShowOutput from(final String id) {
        return new AddSectionToShowOutput(id);
    }

    public static AddSectionToShowOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
