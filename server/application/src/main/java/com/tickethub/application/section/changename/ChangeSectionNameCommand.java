package com.tickethub.application.section.changename;


public record ChangeSectionNameCommand(String id, String name) {
    public static ChangeSectionNameCommand with(final String id, final String name) {
        return new ChangeSectionNameCommand(id, name);
    }
}
