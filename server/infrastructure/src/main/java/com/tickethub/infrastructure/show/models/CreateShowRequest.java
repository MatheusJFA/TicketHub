package com.tickethub.infrastructure.show.models;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record CreateShowRequest(String partnerId, String name, String description, OffsetDateTime date, AddressModel address, Long totalSpots) {
    public CreateShowRequest {
        totalSpots = totalSpots == null ? 0L : totalSpots;
    }
}
