package com.tickethub.application.section.changedescription;


public record ChangeSectionDescriptionCommand(String id, String description) {
    public static ChangeSectionDescriptionCommand with(final String id, final String description) {
        return new ChangeSectionDescriptionCommand(id, description);
    }
}
