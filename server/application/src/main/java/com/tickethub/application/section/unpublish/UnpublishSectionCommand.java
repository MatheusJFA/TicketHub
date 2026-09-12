package com.tickethub.application.section.unpublish;

public record UnpublishSectionCommand(String id) {
    public static UnpublishSectionCommand with(final String id) {
        return new UnpublishSectionCommand(id);
    }
}
