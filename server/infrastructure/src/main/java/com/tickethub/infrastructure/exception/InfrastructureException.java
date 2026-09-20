package com.tickethub.infrastructure.exception;

/**
 * Base for failures in infrastructure adapters (downstream providers,
 * messaging, persistence wiring). Maps to 503 so callers can tell a
 * broken dependency apart from a rejected request (422) or a bug (500).
 */
public class InfrastructureException extends RuntimeException {

    public InfrastructureException(final String message) {
        super(message);
    }

    public InfrastructureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
