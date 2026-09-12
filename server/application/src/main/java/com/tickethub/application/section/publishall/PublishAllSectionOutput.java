package com.tickethub.application.section.publishall;

import com.tickethub.domain.core.section.Section;

public record PublishAllSectionOutput(String id) {
    public static PublishAllSectionOutput from(final String id) {
        return new PublishAllSectionOutput(id);
    }

    public static PublishAllSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
