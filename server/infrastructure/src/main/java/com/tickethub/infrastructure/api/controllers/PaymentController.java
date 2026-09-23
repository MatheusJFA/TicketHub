package com.tickethub.infrastructure.api.controllers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.application.payment.confirm.ConfirmPaymentCommand;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.PaymentAPI;
import com.tickethub.infrastructure.cache.TickethubCacheProperties;
import com.tickethub.infrastructure.notification.OrderConfirmationMailer;
import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.WebhookRequest;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

/**
 * Legacy generic webhook (chargeId + status trusted from the body), kept for
 * local development without a real provider. Disable in production with
 * {@code GENERIC_WEBHOOK_ENABLED=false}: with a real {@code PaymentGateway}
 * a forged body would settle orders without provider approval.
 */
@RestController
@ConditionalOnProperty(prefix = "tickethub.payment", name = "generic-webhook-enabled",
        havingValue = "true", matchIfMissing = true)
public class PaymentController implements PaymentAPI {
    private final ConfirmPaymentUseCase confirmPayment;
    private final OrderConfirmationMailer confirmationMailer;

    public PaymentController(final ConfirmPaymentUseCase confirmPayment,
            final OrderConfirmationMailer confirmationMailer) {
        this.confirmPayment = confirmPayment;
        this.confirmationMailer = confirmationMailer;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = TickethubCacheProperties.SHOWS, allEntries = true),
            @CacheEvict(value = TickethubCacheProperties.SECTIONS, allEntries = true),
            @CacheEvict(value = TickethubCacheProperties.SPOTS, allEntries = true) })
    public ResponseEntity<ConfirmPaymentResponse> paymentWebhook(final WebhookRequest input) {
        final var output = HttpResults.require(confirmPayment.execute(
                ConfirmPaymentCommand.with(input.chargeId(), input.status())));
        if ("PAID".equals(output.orderStatus())) {
            confirmationMailer.sendFor(output.orderId());
        }
        return ResponseEntity.ok(ConfirmPaymentResponse.from(output));
    }
}
