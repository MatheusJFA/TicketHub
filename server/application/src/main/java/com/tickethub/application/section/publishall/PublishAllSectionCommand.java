package com.tickethub.application.section.publishall;

public record PublishAllSectionCommand(String id) {
    public static PublishAllSectionCommand with(final String id) {
        return new PublishAllSectionCommand(id);
    }
}
