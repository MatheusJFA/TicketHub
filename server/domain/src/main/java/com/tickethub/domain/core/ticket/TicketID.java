package com.tickethub.domain.core.ticket;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Identifier;
import java.util.UUID;

public class TicketID extends Identifier {
    private final String value;

    private TicketID(String value) {
        this.value = requireNonNull(value, "'TicketID' should not be null");
    }

    public static TicketID generate() {
        UUID uuid = UUID.randomUUID();
        return new TicketID(uuid.toString());
    }

    public static TicketID from(String value) {
        return new TicketID(value);
    }

    @Override
    public String getValue() {
        return value;
    }
}
