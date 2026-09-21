package com.tickethub.domain.exception;

/**
 * A ticket was already checked in at the door and cannot be used again.
 */
public class TicketAlreadyUsedException extends DomainException {

    public TicketAlreadyUsedException() {
        super("Ticket is already used");
    }
}
