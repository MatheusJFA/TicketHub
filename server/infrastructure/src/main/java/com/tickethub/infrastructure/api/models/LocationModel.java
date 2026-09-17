package com.tickethub.infrastructure.api.models;

import com.tickethub.domain.shared.Location;

public record LocationModel(String value) {
    public Location toDomain() {
        return value == null ? null : Location.create(value);
    }

    public static LocationModel from(Location value) {
        return value == null ? null : new LocationModel(value.getValue());
    }
}
