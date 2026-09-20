package com.tickethub.domain.core.section;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class SectionID extends Identifier {
    private final String value;

    private SectionID(String value) {
        this.value = requireNonNull(value, "'SectionID' should not be null");
    }

    public static SectionID generate() {
        UUID uuid = UUID.randomUUID();
        return new SectionID(uuid.toString());
    }

    public static SectionID from(String value) {
        return new SectionID(value);
    }

    @Override
    public String getValue() {
        return value;
    }

}