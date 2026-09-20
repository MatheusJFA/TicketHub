package com.tickethub.domain.core.show;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class ShowID extends Identifier {
    private final String value;

    private ShowID(String value) {
        this.value = requireNonNull(value, "'ShowID' should not be null");
    }

    public static ShowID generate() {
        UUID uuid = UUID.randomUUID();
        return new ShowID(uuid.toString());
    }

    public static ShowID from(String value) {
        return new ShowID(value);
    }

    @Override
    public String getValue() {
        return value;
    }

}