package com.tickethub.domain.core.customer;

import static java.util.Objects.requireNonNull;
import java.util.UUID;

import com.tickethub.domain.Identifier;

public class CustomerID extends Identifier {
    private final String value;

    private CustomerID(String value) {
        this.value = requireNonNull(value, "'CustomerID' should not be null");
    }

    public static CustomerID generate() {
        UUID uuid = UUID.randomUUID();
        return new CustomerID(uuid.toString());
    }

    public static CustomerID from(String value) {
        return new CustomerID(value);
    }

    @Override
    public String getValue() {
        return value;
    }

}
