package com.tickethub.domain.core.ticket;

import static java.util.Objects.isNull;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.ValidationHandler;
import com.tickethub.domain.validation.Validator;

public final class TicketValidator extends Validator {
    private final Ticket ticket;

    public TicketValidator(final Ticket ticket, final ValidationHandler handler) {
        super(handler);
        this.ticket = ticket;
    }

    @Override
    public void validate() {
        final ValidationHandler handler = validationHandler();

        if (isNull(ticket.getOrderId())) {
            handler.append(new Error("'orderId' should not be null"));
        }
        if (isNull(ticket.getSpotId())) {
            handler.append(new Error("'spotId' should not be null"));
        }
        if (isNull(ticket.getCustomerId())) {
            handler.append(new Error("'customerId' should not be null"));
        }
        if (isNull(ticket.getCode()) || ticket.getCode().isBlank()) {
            handler.append(new Error("'code' should not be blank"));
        }
        if (isNull(ticket.getSignature()) || ticket.getSignature().isBlank()) {
            handler.append(new Error("'signature' should not be blank"));
        }
        if (isNull(ticket.getStatus())) {
            handler.append(new Error("'status' should not be null"));
        }
    }
}
