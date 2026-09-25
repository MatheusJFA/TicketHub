package com.tickethub.infrastructure.webhooks;

public class PartnerWebhookException extends RuntimeException {

    public PartnerWebhookException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
