package com.tickethub.application.section.update;

import com.tickethub.domain.core.section.Section;

public record UpdateSectionOutput(String id) {
    public static UpdateSectionOutput from(final String id) {
        return new UpdateSectionOutput(id);
    }

    public static UpdateSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
