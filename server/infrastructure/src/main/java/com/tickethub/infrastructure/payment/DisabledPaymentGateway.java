package com.tickethub.infrastructure.payment;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.exception.InfrastructureException;

/**
 * Fallback when no payment provider is configured (blank
 * {@code MERCADOPAGO_ACCESS_TOKEN} in local dev). Keeps the boot healthy
 * while pay/reconcile paths fail closed with 503 instead of crashing
 * startup. Mirrors how {@code MercadoPagoWebhookController} answers 503
 * when its handler is absent.
 */
public class DisabledPaymentGateway implements PaymentGateway {

    @Override
    public Charge createCharge(final OrderID orderId, final Money total) {
        throw disabled();
    }

    @Override
    public Charge findStatus(final ChargeID chargeId) {
        throw disabled();
    }

    @Override
    public void refund(final ChargeID chargeId) {
        throw disabled();
    }

    private static InfrastructureException disabled() {
        return new InfrastructureException("Payment provider is not configured: set MERCADOPAGO_ACCESS_TOKEN");
    }
}
