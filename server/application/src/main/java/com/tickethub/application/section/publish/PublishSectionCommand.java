package com.tickethub.application.section.publish;

public record PublishSectionCommand(String id) {
    public static PublishSectionCommand with(final String id) {
        return new PublishSectionCommand(id);
    }
}
