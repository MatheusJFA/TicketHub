package com.tickethub.application.section.create;

import com.tickethub.domain.shared.Money;

public record CreateSectionCommand(String name, String description, long totalSpots, Money price) {
    public static CreateSectionCommand with(final String name, final String description, final long totalSpots, final Money price) {
        return new CreateSectionCommand(name, description, totalSpots, price);
    }
}
