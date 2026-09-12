package com.tickethub.application.section.create;

import com.tickethub.domain.core.section.Section;

public record CreateSectionOutput(String id) {
    public static CreateSectionOutput from(final String id) {
        return new CreateSectionOutput(id);
    }

    public static CreateSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
