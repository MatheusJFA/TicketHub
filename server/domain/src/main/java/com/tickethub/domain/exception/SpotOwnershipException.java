package com.tickethub.domain.exception;

/**
 * A ticket presented at the door belongs to a different show or section
 * than the one being controlled.
 */
public class SpotOwnershipException extends DomainException {

    public SpotOwnershipException() {
        super("Spot does not belong to the given show and section");
    }
}
