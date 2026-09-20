package com.tickethub.infrastructure.exception;

/**
 * A domain event could not be handed to the message broker.
 */
public class EventPublishException extends InfrastructureException {

    public EventPublishException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
