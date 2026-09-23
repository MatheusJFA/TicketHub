package com.tickethub.infrastructure.ticket;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketID;
import com.tickethub.domain.core.ticket.TicketStatus;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.ticket.persistence.TicketDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

@IntegrationTest
@DisplayName("Ticket Mongo gateway")
class TicketMongoGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TicketMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, TicketDocument.COLLECTION);
    }

    private Ticket givenTicket() {
        return Ticket.issue(
                OrderID.generate(), SpotID.generate(), CustomerID.generate(), payload -> "signed:" + payload);
    }

    @Test
    @DisplayName("Given a ticket, when create, then persists and round trips")
    void givenATicket_whenCreate_thenPersistsAndRoundTrips() {
        final var ticket = givenTicket();

        gateway.create(ticket);

        final var found = gateway.findById(ticket.getId()).orElseThrow();
        assertEquals(ticket.getOrderId(), found.getOrderId());
        assertEquals(ticket.getSpotId(), found.getSpotId());
        assertEquals(ticket.getCustomerId(), found.getCustomerId());
        assertEquals(ticket.getCode(), found.getCode());
        assertEquals(ticket.getSignature(), found.getSignature());
        assertEquals(TicketStatus.ISSUED, found.getStatus());
        assertFalse(gateway.findById(TicketID.generate()).isPresent());
    }

    @Test
    @DisplayName("Given a ticket, when update after check in, then persists used status")
    void givenATicket_whenUpdateAfterCheckIn_thenPersistsUsedStatus() {
        final var ticket = gateway.create(givenTicket());

        ticket.checkIn();
        gateway.update(ticket);

        final var found = gateway.findById(ticket.getId()).orElseThrow();
        assertEquals(TicketStatus.USED, found.getStatus());
        assertTrue(ticket.domainEvents().stream()
                .anyMatch(event -> event instanceof com.tickethub.domain.core.ticket.TicketCheckedIn));
    }
}
