package com.tickethub.application.section.unpublishall;

import com.tickethub.domain.core.section.Section;

public record UnpublishAllSectionOutput(String id) {
    public static UnpublishAllSectionOutput from(final String id) {
        return new UnpublishAllSectionOutput(id);
    }

    public static UnpublishAllSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
