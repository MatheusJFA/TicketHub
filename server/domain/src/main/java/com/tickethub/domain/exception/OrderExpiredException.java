package com.tickethub.domain.exception;

/**
 * An order was already expired (reservation TTL elapsed) and can no longer be paid.
 */
public class OrderExpiredException extends DomainException {

    public OrderExpiredException() {
        super("Order is expired");
    }
}
