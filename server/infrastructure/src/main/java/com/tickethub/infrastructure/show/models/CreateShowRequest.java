package com.tickethub.infrastructure.show.models;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.time.OffsetDateTime;
import com.tickethub.infrastructure.api.models.*;

public record CreateShowRequest(String partnerId, String name, String description, OffsetDateTime date, AddressModel address, Long totalSpots) {
    public CreateShowRequest {
        totalSpots = defaultIfNull(totalSpots, 0L);
    }
}
