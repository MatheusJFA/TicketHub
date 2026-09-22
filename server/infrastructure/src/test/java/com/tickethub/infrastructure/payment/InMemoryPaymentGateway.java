package com.tickethub.infrastructure.payment;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.payment.Charge;
import com.tickethub.domain.core.payment.ChargeID;
import com.tickethub.domain.core.payment.ChargeStatus;
import com.tickethub.domain.core.payment.PaymentGateway;
import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.shared.Money;

/**
 * In-memory {@link PaymentGateway} for full-context tests (IT/E2E/Cucumber),
 * where no real provider credential exists. Charges stay PENDING until the
 * test flips them; mirrors the deleted MockPaymentGateway contract without
 * shipping test doubles in production code.
 */
public class InMemoryPaymentGateway implements PaymentGateway {

    private final Map<String, Charge> charges = new ConcurrentHashMap<>();

    @Override
    public Charge createCharge(final OrderID orderId, final Money total) {
        requireNonNull(orderId, "'orderId' should not be null");
        requireNonNull(total, "'total' should not be null");
        final var chargeId = ChargeID.from("ch_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        final var charge = Charge.create(chargeId, orderId, total, ChargeStatus.PENDING,
                "PIX-TEST-" + chargeId.getValue());
        charges.put(chargeId.getValue(), charge);
        return charge;
    }

    @Override
    public Charge findStatus(final ChargeID chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        return Optional.ofNullable(charges.get(chargeId.getValue()))
                .orElseThrow(() -> new ResourceNotFoundException("Charge", chargeId.getValue()));
    }

    @Override
    public void refund(final ChargeID chargeId) {
        requireNonNull(chargeId, "'chargeId' should not be null");
        final var charge = findStatus(chargeId);
        if (charge.getStatus() == ChargeStatus.PENDING) {
            charge.markAsFailed();
        }
    }
}
