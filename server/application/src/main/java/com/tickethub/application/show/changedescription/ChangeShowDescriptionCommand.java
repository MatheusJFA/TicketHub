package com.tickethub.application.show.changedescription;

public record ChangeShowDescriptionCommand(String id, String description) {
    public static ChangeShowDescriptionCommand with(final String id, final String description) {
        return new ChangeShowDescriptionCommand(id, description);
    }
}
