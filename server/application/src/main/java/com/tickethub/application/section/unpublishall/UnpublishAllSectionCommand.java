package com.tickethub.application.section.unpublishall;

public record UnpublishAllSectionCommand(String id) {
    public static UnpublishAllSectionCommand with(final String id) {
        return new UnpublishAllSectionCommand(id);
    }
}
