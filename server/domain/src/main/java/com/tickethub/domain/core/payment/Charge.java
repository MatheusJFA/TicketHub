package com.tickethub.domain.core.payment;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.shared.Money;

/**
 * A payment charge for an order. Created through {@link PaymentGateway} when
 * the buyer starts paying; confirmed later through the provider webhook.
 * {@code paymentCode} carries the provider-specific payload the buyer uses
 * to pay (for example a PIX copy-and-paste code).
 */
public record Charge(
        String chargeId,
        OrderID orderId,
        Money total,
        ChargeStatus status,
        String paymentCode) {

    public Charge {
        requireNonNull(chargeId, "'chargeId' should not be null");
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(total, "'total' should not be null");
        requireNonNull(status, "'status' should not be null");
    }
}
