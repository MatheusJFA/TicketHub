package com.tickethub.application.show.changedescription;

import com.tickethub.domain.core.show.Show;

public record ChangeShowDescriptionOutput(String id) {
    public static ChangeShowDescriptionOutput from(final String id) {
        return new ChangeShowDescriptionOutput(id);
    }

    public static ChangeShowDescriptionOutput from(final Show entity) {
        return from(entity.getId().getValue());
    }
}
