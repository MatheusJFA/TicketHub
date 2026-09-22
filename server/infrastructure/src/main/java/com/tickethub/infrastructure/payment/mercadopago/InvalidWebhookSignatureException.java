package com.tickethub.infrastructure.payment.mercadopago;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InvalidWebhookSignatureException extends ResponseStatusException {

    public InvalidWebhookSignatureException(final String reason) {
        super(HttpStatus.UNAUTHORIZED, reason);
    }
}
