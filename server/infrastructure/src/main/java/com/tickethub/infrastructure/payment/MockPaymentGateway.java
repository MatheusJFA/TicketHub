package com.tickethub.infrastructure.payment;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.shared.Money;

/**
 * Deterministic in-process payment provider for development and tests.
 * Charges stay PENDING until the simulated provider callback flips them
 * through {@code POST /payments/webhook}; swap this adapter for a real PSP
 * without touching the domain or the use cases.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    private final Map<String, Charge> charges = new ConcurrentHashMap<>();

    @Override
    public Charge createCharge(final OrderID orderId, final Money total) {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(total, "'total' should not be null");
        final var chargeId = ChargeID.from("ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        final var charge = Charge.create(chargeId, orderId, total, ChargeStatus.PENDING,
                "PIX-MOCK-" + chargeId.getValue());
        charges.put(chargeId.getValue(), charge);
        return charge;
    }

    @Override
    public Charge findStatus(final ChargeID chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        final var charge = charges.get(chargeId.getValue());
        if (charge == null) {
            throw new ResourceNotFoundException("Charge", chargeId.getValue());
        }
        return charge;
    }
}
