package com.tickethub.application.section.update;

import com.tickethub.domain.shared.Money;

public record UpdateSectionCommand(String id, String name, String description, Money price) {
    public static UpdateSectionCommand with(final String id, final String name, final String description,
            final Money price) {
        return new UpdateSectionCommand(id, name, description, price);
    }
}
