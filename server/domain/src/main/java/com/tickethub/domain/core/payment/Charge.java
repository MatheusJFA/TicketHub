package com.tickethub.domain.core.payment;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.Entity;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.exception.IllegalChargeTransitionException;
import com.tickethub.domain.shared.Money;

/**
 * A payment charge for an order. Created through {@link PaymentGateway} when
 * the buyer starts paying; confirmed later through the provider webhook.
 * {@code paymentCode} carries the provider-specific payload the buyer uses
 * to pay (for example a PIX copy-and-paste code).
 */
public class Charge extends Entity<ChargeID> {
    private final OrderID orderId;
    private final Money total;
    private ChargeStatus status;
    private final String paymentCode;

    private Charge(final ChargeID chargeId, final OrderID orderId, final Money total,
            final ChargeStatus status, final String paymentCode) {
        super(chargeId);
        this.orderId = requireNonNull(orderId, "'orderId' should not be null");
        this.total = requireNonNull(total, "'total' should not be null");
        this.status = requireNonNull(status, "'status' should not be null");
        this.paymentCode = paymentCode;
    }

    public static Charge create(final ChargeID chargeId, final OrderID orderId, final Money total,
            final ChargeStatus status, final String paymentCode) {
        return new Charge(chargeId, orderId, total, status, paymentCode);
    }

    public ChargeID getChargeId() {
        return getId();
    }

    public OrderID getOrderId() {
        return orderId;
    }

    public Money getTotal() {
        return total;
    }

    public ChargeStatus getStatus() {
        return status;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    /**
     * Moves the charge through its state machine ({@link ChargeStatus}).
     * Only PENDING charges move, exactly once, to PAID or FAILED; anything
     * else (including repeating the current status) is rejected.
     */
    public void changeStatus(final ChargeStatus status) {
        requireNonNull(status, "'status' should not be null");
        if (!this.status.canTransitionTo(status)) {
            throw new IllegalChargeTransitionException(this.status, status);
        }
        this.status = status;
        markAsUpdated();
    }

    public void markAsPaid() {
        changeStatus(ChargeStatus.PAID);
    }

    public void markAsFailed() {
        changeStatus(ChargeStatus.FAILED);
    }

    @Override
    public String toString() {
        return "Charge[chargeId=" + getId().getValue() + ", orderId=" + orderId.getValue()
                + ", total=" + total + ", status=" + status + "]";
    }
}
