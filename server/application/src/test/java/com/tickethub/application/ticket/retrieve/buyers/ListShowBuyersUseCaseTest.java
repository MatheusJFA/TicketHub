package com.tickethub.application.ticket.retrieve.buyers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.customer.Customer;
import com.tickethub.domain.core.customer.CustomerGateway;
import com.tickethub.domain.core.order.OrderID;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.ticket.Ticket;
import com.tickethub.domain.core.ticket.TicketGateway;
import com.tickethub.domain.shared.Location;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("List show buyers use case")
class ListShowBuyersUseCaseTest extends UseCaseTest {

    private final TicketGateway ticketGateway = mock(TicketGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final CustomerGateway customerGateway = mock(CustomerGateway.class);
    private final DefaultListShowBuyersUseCase useCase =
            new DefaultListShowBuyersUseCase(ticketGateway, spotGateway, customerGateway);

    @Override
    protected List<Object> getMocks() {
        return List.of(ticketGateway, spotGateway, customerGateway);
    }

    @Test
    @DisplayName("Given show tickets, when execute, then returns buyers enriched")
    void givenShowTickets_whenExecute_thenReturnsBuyersEnriched() {
        final var showId = ShowID.generate();
        final var spot = Spot.create(Location.create("A00001"));
        final var customer =
                Customer.create("52998224725", "Maria Silva", "maria@mail.com", "$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2");
        final var ticket = Ticket.issue(OrderID.generate(), spot.getId(), customer.getId(), payload -> "sig");
        when(spotGateway.findByShowId(showId)).thenReturn(List.of(spot));
        when(ticketGateway.findBySpotIds(List.of(spot.getId()))).thenReturn(List.of(ticket));
        when(customerGateway.findById(customer.getId())).thenReturn(Optional.of(customer));

        final var output = useCase.execute(ListShowBuyersCommand.with(showId.getValue())).getRight();

        assertNotNull(output);
        assertEquals(1, output.size());
        assertEquals(ticket.getId().getValue(), output.get(0).ticketId());
        assertEquals("A00001", output.get(0).location());
        assertEquals("Maria Silva", output.get(0).buyerName());
        assertEquals("52998224725", output.get(0).buyerCpf());
        assertEquals("maria@mail.com", output.get(0).buyerEmail());
        verify(spotGateway, times(1)).findByShowId(showId);
        verify(ticketGateway, times(1)).findBySpotIds(List.of(spot.getId()));
        verify(customerGateway, times(1)).findById(customer.getId());
    }

    @Test
    @DisplayName("Given orphan ticket, when execute, then skips it")
    void givenOrphanTicket_whenExecute_thenSkipsIt() {
        final var showId = ShowID.generate();
        final var spot = Spot.create(Location.create("A00001"));
        final var customer =
                Customer.create("52998224725", "Maria Silva", "maria@mail.com", "$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2");
        final var ticket = Ticket.issue(OrderID.generate(), spot.getId(), customer.getId(), payload -> "sig");
        when(spotGateway.findByShowId(showId)).thenReturn(List.of());
        when(ticketGateway.findBySpotIds(List.of())).thenReturn(List.of());

        final var output = useCase.execute(ListShowBuyersCommand.with(showId.getValue())).getRight();

        assertNotNull(output);
        assertTrue(output.isEmpty());
        verify(spotGateway, times(1)).findByShowId(showId);
        verify(ticketGateway, times(1)).findBySpotIds(List.of());
    }

    @Test
    @DisplayName("Given gateway failure, when execute, then returns left")
    void givenGatewayFailure_whenExecute_thenReturnsLeft() {
        final var showId = ShowID.generate();
        when(spotGateway.findByShowId(any())).thenThrow(new RuntimeException("boom"));

        final var notification =
                useCase.execute(ListShowBuyersCommand.with(showId.getValue())).getLeft();

        assertNotNull(notification);
        verify(spotGateway, times(1)).findByShowId(any());
    }
}
