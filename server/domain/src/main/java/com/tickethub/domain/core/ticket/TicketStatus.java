package com.tickethub.domain.core.ticket;

/**
 * Ticket lifecycle. Tickets are ISSUED for paid orders and become USED at
 * door check-in. A used ticket can never be reused.
 */
public enum TicketStatus {
    ISSUED,
    USED
}
