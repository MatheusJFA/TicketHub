package com.tickethub.infrastructure.exception;

/**
 * An inbound spot generation message could not be parsed or applied.
 * Thrown so the broker retries delivery.
 */
public class SpotGenerationException extends InfrastructureException {

    public SpotGenerationException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
