package com.tickethub.domain.exception;

/**
 * A ticket presented at the door failed signature verification, so it was
 * not issued by this system (or was tampered with).
 */
public class InvalidTicketSignatureException extends DomainException {

    public InvalidTicketSignatureException() {
        super("Invalid ticket signature");
    }
}
