package com.tickethub.application.show.create;

import java.time.OffsetDateTime;
import com.tickethub.domain.shared.Address;

public record CreateShowCommand(String partnerId, String name, String description, OffsetDateTime date, Address address, long totalSpots) {
    public static CreateShowCommand with(final String partnerId, final String name, final String description, final OffsetDateTime date, final Address address, final long totalSpots) {
        return new CreateShowCommand(partnerId, name, description, date, address, totalSpots);
    }
}
