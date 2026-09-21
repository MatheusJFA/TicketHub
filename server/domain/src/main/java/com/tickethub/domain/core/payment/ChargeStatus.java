package com.tickethub.domain.core.payment;

/**
 * Charge lifecycle as reported by the payment provider. The provider is the
 * source of truth; the order mirrors it through the webhook confirmation.
 *
 * <p>State machine: a charge starts PENDING and moves exactly once, to PAID
 * or FAILED. Both are terminal; any other transition (including repeating
 * the current status) is rejected so double callbacks cannot corrupt state.
 */
public enum ChargeStatus {
    PENDING,
    PAID,
    FAILED;

    public boolean canTransitionTo(final ChargeStatus target) {
        return this == PENDING && (target == PAID || target == FAILED);
    }
}
