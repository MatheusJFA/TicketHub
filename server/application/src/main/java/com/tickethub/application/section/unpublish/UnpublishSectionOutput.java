package com.tickethub.application.section.unpublish;

import com.tickethub.domain.core.section.Section;

public record UnpublishSectionOutput(String id) {
    public static UnpublishSectionOutput from(final String id) {
        return new UnpublishSectionOutput(id);
    }

    public static UnpublishSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
