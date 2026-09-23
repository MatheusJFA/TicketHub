package com.tickethub.application.show.addsection;

import com.tickethub.domain.shared.Money;

public record AddSectionToShowCommand(String showId, String name, String description, long totalSpots, Money price) {
    public static AddSectionToShowCommand with(
            final String showId,
            final String name,
            final String description,
            final long totalSpots,
            final Money price) {
        return new AddSectionToShowCommand(showId, name, description, totalSpots, price);
    }
}
