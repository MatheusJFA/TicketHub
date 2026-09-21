package com.tickethub.domain.exception;

import com.tickethub.domain.core.payment.ChargeStatus;

/**
 * A charge transition was requested from a status that does not allow it
 * (for example paying an already failed charge).
 */
public class IllegalChargeTransitionException extends DomainException {

    public IllegalChargeTransitionException(final ChargeStatus from, final ChargeStatus to) {
        super("Illegal charge transition from " + from + " to " + to);
    }
}
