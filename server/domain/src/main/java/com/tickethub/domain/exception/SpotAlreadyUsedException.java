package com.tickethub.domain.exception;

/**
 * A spot was already checked in and cannot be used again.
 */
public class SpotAlreadyUsedException extends DomainException {

    public SpotAlreadyUsedException() {
        super("Spot is already used");
    }
}
