package com.tickethub.domain.core.spot;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class SpotID extends Identifier {
    private final String value;

    private SpotID(String value) {
        this.value = requireNonNull(value, "'SpotID' should not be null");
    }

    public static SpotID generate() {
        UUID uuid = UUID.randomUUID();
        return new SpotID(uuid.toString());
    }

    public static SpotID from(String value) {
        return new SpotID(value);
    }

    @Override
    public String getValue() {
        return value;
    }

}