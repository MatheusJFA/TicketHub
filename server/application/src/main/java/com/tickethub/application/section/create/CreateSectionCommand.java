package com.tickethub.application.section.create;

import com.tickethub.domain.shared.Money;

public record CreateSectionCommand(String showId, String name, String description, long totalSpots, Money price) {
    public static CreateSectionCommand with(
            final String showId,
            final String name,
            final String description,
            final long totalSpots,
            final Money price) {
        return new CreateSectionCommand(showId, name, description, totalSpots, price);
    }
}
