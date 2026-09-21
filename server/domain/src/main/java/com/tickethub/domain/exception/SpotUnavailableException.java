package com.tickethub.domain.exception;

/**
 * A spot is already reserved, sold or used, so it cannot be reserved again.
 */
public class SpotUnavailableException extends DomainException {

    public SpotUnavailableException() {
        super("Spot is unavailable");
    }
}
