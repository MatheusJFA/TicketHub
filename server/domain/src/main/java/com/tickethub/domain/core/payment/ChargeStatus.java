package com.tickethub.domain.core.payment;

/**
 * Charge lifecycle as reported by the payment provider. The provider is the
 * source of truth; the order mirrors it through the webhook confirmation.
 */
public enum ChargeStatus {
    PENDING,
    PAID,
    FAILED
}
