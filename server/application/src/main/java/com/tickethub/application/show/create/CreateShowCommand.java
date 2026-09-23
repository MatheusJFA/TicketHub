package com.tickethub.application.show.create;

import com.tickethub.domain.shared.Address;
import java.time.OffsetDateTime;

public record CreateShowCommand(
        String partnerId, String name, String description, OffsetDateTime date, Address address, long totalSpots) {
    public static CreateShowCommand with(
            final String partnerId,
            final String name,
            final String description,
            final OffsetDateTime date,
            final Address address,
            final long totalSpots) {
        return new CreateShowCommand(partnerId, name, description, date, address, totalSpots);
    }
}
