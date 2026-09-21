package com.tickethub.infrastructure.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.tickethub.application.payment.confirm.ConfirmPaymentCommand;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.infrastructure.api.HttpResults;
import com.tickethub.infrastructure.api.PaymentAPI;
import com.tickethub.infrastructure.payment.models.ConfirmPaymentResponse;
import com.tickethub.infrastructure.payment.models.WebhookRequest;

@RestController
public class PaymentController implements PaymentAPI {
    private final ConfirmPaymentUseCase confirmPayment;

    public PaymentController(final ConfirmPaymentUseCase confirmPayment) {
        this.confirmPayment = confirmPayment;
    }

    @Override
    public ResponseEntity<ConfirmPaymentResponse> paymentWebhook(final WebhookRequest input) {
        final var output = HttpResults.require(confirmPayment.execute(
                ConfirmPaymentCommand.with(input.chargeId(), input.status())));
        return ResponseEntity.ok(ConfirmPaymentResponse.from(output));
    }
}
