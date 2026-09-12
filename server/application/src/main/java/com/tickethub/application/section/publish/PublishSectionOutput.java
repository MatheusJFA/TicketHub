package com.tickethub.application.section.publish;

import com.tickethub.domain.core.section.Section;

public record PublishSectionOutput(String id) {
    public static PublishSectionOutput from(final String id) {
        return new PublishSectionOutput(id);
    }

    public static PublishSectionOutput from(final Section entity) {
        return from(entity.getId().getValue());
    }
}
