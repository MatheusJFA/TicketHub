package com.tickethub.domain.core.partner;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class PartnerID extends Identifier {
    private final String value;

    private PartnerID(String value) {
        this.value = requireNonNull(value, "'PartnerID' should not be null");
    }

    public static PartnerID generate() {
        UUID uuid = UUID.randomUUID();
        return new PartnerID(uuid.toString());
    }

    public static PartnerID from(String value) {
        return new PartnerID(value);
    }

    @Override
    public String getValue() {
        return value;
    }

}