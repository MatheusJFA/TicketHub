package com.tickethub.domain.core.payment;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class ChargeID extends Identifier {
    private final String value;

    private ChargeID(String value) {
        this.value = requireNonNull(value, "'ChargeID' should not be null");
    }

    public static ChargeID generate() {
        UUID uuid = UUID.randomUUID();
        return new ChargeID(uuid.toString());
    }

    public static ChargeID from(String value) {
        return new ChargeID(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}
