package com.tickethub.domain.exception;

import com.tickethub.domain.core.order.OrderStatus;

/**
 * An order transition was requested from a status that does not allow it
 * (for example paying an already cancelled order).
 */
public class IllegalOrderTransitionException extends DomainException {

    public IllegalOrderTransitionException(final OrderStatus from, final OrderStatus to) {
        super("Illegal order transition from " + from + " to " + to);
    }
}
