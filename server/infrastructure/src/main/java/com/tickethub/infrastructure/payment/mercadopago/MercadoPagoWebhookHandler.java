package com.tickethub.infrastructure.payment.mercadopago;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.nonNull;

import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.application.payment.confirm.ConfirmPaymentCommand;
import com.tickethub.application.payment.confirm.ConfirmPaymentOutput;
import com.tickethub.application.payment.confirm.ConfirmPaymentUseCase;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

/**
 * Handles Mercado Pago payment notifications with verify-then-fetch: the
 * signature is checked first, then the payment status is read from the MP
 * API — never trusted from the callback body. Unknown payments map to 404
 * through the {@code "not found"} convention. Non-payment topics are ignored
 * with an empty result (caller answers 200 so MP stops retrying).
 */
public class MercadoPagoWebhookHandler {

    private final MercadoPagoWebhookVerifier verifier;
    private final PaymentGateway paymentGateway;
    private final ConfirmPaymentUseCase confirmPayment;

    public MercadoPagoWebhookHandler(final MercadoPagoWebhookVerifier verifier,
            final PaymentGateway paymentGateway, final ConfirmPaymentUseCase confirmPayment) {
        this.verifier = requireNonNull(verifier, "'verifier' should not be null");
        this.paymentGateway = requireNonNull(paymentGateway, "'paymentGateway' should not be null");
        this.confirmPayment = requireNonNull(confirmPayment, "'confirmPayment' should not be null");
    }

    public Optional<Either<Notification, ConfirmPaymentOutput>> handle(final String xSignature,
            final String xRequestId, final String dataId, final String type) {
        if (nonNull(type) && !"payment".equalsIgnoreCase(type)) {
            return Optional.empty();
        }
        verifier.verify(xSignature, xRequestId, dataId);
        final String status;
        try {
            status = paymentGateway.findStatus(ChargeID.from(dataId)).getStatus().name();
        } catch (final ResourceNotFoundException e) {
            return Optional.of(Either.left(
                    Notification.create(new Error("Charge not found: " + dataId))));
        }
        return Optional.of(confirmPayment.execute(ConfirmPaymentCommand.with(dataId, status)));
    }
}
