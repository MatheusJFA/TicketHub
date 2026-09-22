package com.tickethub.infrastructure.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.MercadoPagoWebhookAPI;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.MercadoPagoNotification;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoWebhookHandler;

import org.springframework.cache.annotation.CacheEvict;

@RestController
public class MercadoPagoWebhookController implements MercadoPagoWebhookAPI {
    private final MercadoPagoWebhookHandler webhook;

    public MercadoPagoWebhookController(final MercadoPagoWebhookHandler webhook) {
        this.webhook = webhook;
    }

    @Override
    @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true)
    public ResponseEntity<ConfirmPaymentResponse> mercadoPagoWebhook(final String xSignature,
            final String xRequestId, final String dataId, final String type,
            final MercadoPagoNotification body) {
        final var paymentId = dataId != null ? dataId
                : body != null ? body.dataId() : null;
        final var topic = type != null ? type
                : body != null ? body.type() : null;
        final var result = webhook.handle(xSignature, xRequestId, paymentId, topic);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return ResponseEntity.ok(ConfirmPaymentResponse.from(HttpResults.require(result.get())));
    }
}
