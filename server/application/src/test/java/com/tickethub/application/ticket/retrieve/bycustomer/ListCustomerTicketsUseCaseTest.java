package com.tickethub.application.ticket.retrieve.bycustomer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.CustomerID;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("List customer tickets use case")
class ListCustomerTicketsUseCaseTest extends UseCaseTest {

    private static final Address ADDRESS =
            Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000");

    private final TicketGateway ticketGateway = mock(TicketGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final ShowGateway showGateway = mock(ShowGateway.class);
    private final DefaultListCustomerTicketsUseCase useCase =
            new DefaultListCustomerTicketsUseCase(ticketGateway, spotGateway, showGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(ticketGateway, spotGateway, showGateway);
    }

    @Test
    @DisplayName("Given customer tickets, when execute, then returns enriched tickets")
    void givenCustomerTickets_whenExecute_thenReturnsEnrichedTickets() {
        final var customerId = CustomerID.generate();
        final var show = Show.create(
                "Concert", "Description", OffsetDateTime.parse("2027-01-15T20:00:00-03:00"), ADDRESS, 10,
                PartnerID.generate());
        final var spot = Spot.create(Location.create("A00001"));
        final var ticket = Ticket.issue(OrderID.generate(), spot.getId(), customerId, payload -> "sig");
        when(ticketGateway.findByCustomerId(customerId)).thenReturn(List.of(ticket));
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, show.getId().getValue(), "section-1")));
        when(showGateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var output = useCase.execute(ListCustomerTicketsCommand.with(customerId.getValue())).getRight();

        assertNotNull(output);
        assertEquals(1, output.size());
        assertEquals(ticket.getId().getValue(), output.get(0).ticketId());
        assertEquals("A00001", output.get(0).location());
        assertEquals("Concert", output.get(0).showName());
        verify(ticketGateway, times(1)).findByCustomerId(customerId);
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
    }

    @Test
    @DisplayName("Given orphan ticket, when execute, then skips it")
    void givenOrphanTicket_whenExecute_thenSkipsIt() {
        final var customerId = CustomerID.generate();
        final var spot = Spot.create(Location.create("A00001"));
        final var ticket = Ticket.issue(OrderID.generate(), spot.getId(), customerId, payload -> "sig");
        when(ticketGateway.findByCustomerId(customerId)).thenReturn(List.of(ticket));
        when(spotGateway.findPlacement(spot.getId())).thenReturn(Optional.empty());

        final var output = useCase.execute(ListCustomerTicketsCommand.with(customerId.getValue())).getRight();

        assertNotNull(output);
        assertTrue(output.isEmpty());
        verify(ticketGateway, times(1)).findByCustomerId(customerId);
        verify(spotGateway, times(1)).findPlacement(spot.getId());
    }

    @Test
    @DisplayName("Given gateway failure, when execute, then returns left")
    void givenGatewayFailure_whenExecute_thenReturnsLeft() {
        final var customerId = CustomerID.generate();
        when(ticketGateway.findByCustomerId(any())).thenThrow(new RuntimeException("boom"));

        final var notification =
                useCase.execute(ListCustomerTicketsCommand.with(customerId.getValue())).getLeft();

        assertNotNull(notification);
        verify(ticketGateway, times(1)).findByCustomerId(any());
    }
}
