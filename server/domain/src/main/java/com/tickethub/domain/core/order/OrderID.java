package com.tickethub.domain.core.order;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class OrderID extends Identifier {
    private final String value;

    private OrderID(String value) {
        this.value = requireNonNull(value, "'OrderID' should not be null");
    }

    public static OrderID generate() {
        UUID uuid = UUID.randomUUID();
        return new OrderID(uuid.toString());
    }

    public static OrderID from(String value) {
        return new OrderID(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}
