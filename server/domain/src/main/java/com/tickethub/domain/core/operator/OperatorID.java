package com.tickethub.domain.core.operator;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class OperatorID extends Identifier {
    private final String value;

    private OperatorID(String value) {
        this.value = requireNonNull(value, "'OperatorID' should not be null");
    }

    public static OperatorID generate() {
        UUID uuid = UUID.randomUUID();
        return new OperatorID(uuid.toString());
    }

    public static OperatorID from(String value) {
        return new OperatorID(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}
