package com.tickethub.domain.core.order;

/**
 * Purchase lifecycle. An order starts PENDING while its spots are reserved;
 * paying moves it to PAID (tickets are then issued). PENDING orders leave
 * the lifecycle through EXPIRED (reservation TTL elapsed) or CANCELLED
 * (buyer gave up). Money captured for a dead order is returned through
 * REFUNDED (from PAID or EXPIRED).
 */
public enum OrderStatus {
    PENDING,
    PAID,
    EXPIRED,
    CANCELLED,
    REFUNDED
}
