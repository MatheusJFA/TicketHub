package com.tickethub.domain.core.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.exception.InvalidTicketSignatureException;
import com.tickethub.domain.exception.TicketAlreadyUsedException;
import com.tickethub.domain.validation.Notification;

@DisplayName("Ticket")
class TicketTest {

    private static final TicketSigner SIGNER = payload -> "signed:" + payload;
    private static final TicketSigner FORGED = payload -> "forged";

    private static Ticket issued() {
        return Ticket.issue(OrderID.generate(), SpotID.generate(), CustomerID.generate(), SIGNER);
    }

    @Test
    @DisplayName("Given order item, when issue, then ticket issued with code and signature")
    void givenOrderItem_whenIssue_thenTicketIssued() {
        final var orderId = OrderID.generate();
        final var spotId = SpotID.generate();
        final var customerId = CustomerID.generate();

        final var ticket = Ticket.issue(orderId, spotId, customerId, SIGNER);

        assertNotNull(ticket.getId());
        assertEquals(orderId, ticket.getOrderId());
        assertEquals(spotId, ticket.getSpotId());
        assertEquals(customerId, ticket.getCustomerId());
        assertNotNull(ticket.getCode());
        assertEquals(8, ticket.getCode().length());
        assertEquals("signed:" + Ticket.signedPayload(ticket.getId().getValue(), ticket.getCode()),
                ticket.getSignature());
        assertEquals(TicketStatus.ISSUED, ticket.getStatus());
        assertEquals(1, ticket.domainEvents().size());
        assertTrue(ticket.domainEvents().get(0) instanceof TicketIssued);
    }

    @Test
    @DisplayName("Given issued ticket, when verify signature, then accept genuine signer")
    void givenIssuedTicket_whenVerifySignature_thenAccept() {
        final var ticket = issued();

        ticket.verifySignature(SIGNER);

        assertEquals(TicketStatus.ISSUED, ticket.getStatus());
    }

    @Test
    @DisplayName("Given forged signer, when verify signature, then throw invalid signature")
    void givenForgedSigner_whenVerifySignature_thenThrowInvalidSignature() {
        final var ticket = issued();

        final var exception = assertThrows(InvalidTicketSignatureException.class,
                () -> ticket.verifySignature(FORGED));

        assertEquals("Invalid ticket signature", exception.getMessage());
    }

    @Test
    @DisplayName("Given issued ticket, when check in, then status used with event")
    void givenIssuedTicket_whenCheckIn_thenStatusUsed() {
        final var ticket = issued();

        ticket.checkIn();

        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertEquals(2, ticket.domainEvents().size());
        assertTrue(ticket.domainEvents().get(1) instanceof TicketCheckedIn);
    }

    @Test
    @DisplayName("Given used ticket, when check in again, then throw already used")
    void givenUsedTicket_whenCheckInAgain_thenThrowAlreadyUsed() {
        final var ticket = issued();
        ticket.checkIn();

        final var exception = assertThrows(TicketAlreadyUsedException.class, ticket::checkIn);

        assertEquals("Ticket is already used", exception.getMessage());
    }

    @Test
    @DisplayName("Given valid ticket, when validate, then no errors")
    void givenValidTicket_whenValidate_thenNoErrors() {
        final var ticket = issued();

        final var notification = Notification.create();
        ticket.validate(notification);

        assertTrue(notification.getErrors().isEmpty());
    }
}
