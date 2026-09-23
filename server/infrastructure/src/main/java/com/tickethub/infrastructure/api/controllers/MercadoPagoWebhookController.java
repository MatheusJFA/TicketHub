package com.tickethub.infrastructure.api.controllers;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.MercadoPagoWebhookAPI;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import com.tickethub.infrastructure.exception.InfrastructureException;
import com.tickethub.infrastructure.notification.OrderConfirmationMailer;
import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.MercadoPagoNotification;
import com.tickethub.infrastructure.payment.mercadopago.MercadoPagoWebhookHandler;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@RestController
public class MercadoPagoWebhookController implements MercadoPagoWebhookAPI {
    private final ObjectProvider<MercadoPagoWebhookHandler> webhook;
    private final OrderConfirmationMailer confirmationMailer;

    public MercadoPagoWebhookController(final ObjectProvider<MercadoPagoWebhookHandler> webhook,
            final OrderConfirmationMailer confirmationMailer) {
        this.webhook = webhook;
        this.confirmationMailer = confirmationMailer;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true),
            @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true),
            @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true) })
    public ResponseEntity<ConfirmPaymentResponse> mercadoPagoWebhook(final String xSignature,
            final String xRequestId, final String dataId, final String type,
            final MercadoPagoNotification body) {
        final var handler = webhook.getIfAvailable();
        if (handler == null) {
            throw new InfrastructureException(
                    "Mercado Pago webhook is not configured: set MERCADOPAGO_ACCESS_TOKEN");
        }
        final var paymentId = dataId != null ? dataId
                : body != null ? body.dataId() : null;
        final var topic = type != null ? type
                : body != null ? body.type() : null;
        final var result = handler.handle(xSignature, xRequestId, paymentId, topic);
        if (result.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        final var output = HttpResults.require(result.get());
        if ("PAID".equals(output.orderStatus())) {
            confirmationMailer.sendFor(output.orderId());
        }
        return ResponseEntity.ok(ConfirmPaymentResponse.from(output));
    }
}
