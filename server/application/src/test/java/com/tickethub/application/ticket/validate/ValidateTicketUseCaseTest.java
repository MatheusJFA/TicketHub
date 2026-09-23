package com.tickethub.application.ticket.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
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
import com.tickethub.domain.core.ticket.TicketID;
import com.tickethub.domain.core.ticket.TicketSigner;
import com.tickethub.domain.core.ticket.TicketStatus;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Validate ticket use case")
class ValidateTicketUseCaseTest extends UseCaseTest {

    private static final Instant NOW = Instant.parse("2027-01-15T22:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final OffsetDateTime TODAY = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final OffsetDateTime YESTERDAY = OffsetDateTime.parse("2027-01-14T20:00:00-03:00");
    private static final Address ADDRESS =
            Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000");
    private static final TicketSigner SIGNER = payload -> "signed:" + payload;

    private final TicketGateway ticketGateway = mock(TicketGateway.class);
    private final SpotGateway spotGateway = mock(SpotGateway.class);
    private final ShowGateway showGateway = mock(ShowGateway.class);
    private final DefaultValidateTicketUseCase useCase =
            new DefaultValidateTicketUseCase(ticketGateway, SIGNER, spotGateway, showGateway, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(ticketGateway, spotGateway, showGateway);
    }

    private Show givenShow(final OffsetDateTime date) {
        return Show.create("Concert", "Description", date, ADDRESS, 10, PartnerID.generate());
    }

    private Ticket givenTicket(final Spot spot) {
        final var ticket = Ticket.issue(OrderID.generate(), spot.getId(), CustomerID.generate(), SIGNER);
        when(ticketGateway.findById(ticket.getId())).thenReturn(Optional.of(ticket));
        when(ticketGateway.update(any())).thenAnswer(returnsFirstArg());
        return ticket;
    }

    private void givenPlacement(final Spot spot, final Show show) {
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, show.getId().getValue(), "section-1")));
        when(showGateway.findById(show.getId())).thenReturn(Optional.of(show));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());
    }

    @Test
    @DisplayName("Given valid QR data for today show, when execute, then checks ticket in")
    void givenValidQrData_whenExecute_thenChecksTicketIn() {
        final var spot = Spot.create(Location.create("A00001"));
        final var show = givenShow(TODAY);
        final var ticket = givenTicket(spot);
        givenPlacement(spot, show);
        final var command = ValidateTicketCommand.with(
                show.getId().getValue(), ticket.getId().getValue(), ticket.getCode(), ticket.getSignature());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output);
        assertEquals(ticket.getId().getValue(), output.ticketId());
        assertEquals(show.getId().getValue(), output.showId());
        assertEquals(TicketStatus.USED, ticket.getStatus());
        assertFalse(spot.isAvailable(), () -> "Spot should be marked as used after check-in");
        verify(ticketGateway, times(1)).findById(ticket.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(ticketGateway, times(1)).update(ticket);
        verify(spotGateway, times(1)).update(spot);
    }

    @Test
    @DisplayName("Given unknown ticket, when execute, then returns not found")
    void givenUnknownTicket_whenExecute_thenReturnsNotFound() {
        final var ticketId = TicketID.generate();
        when(ticketGateway.findById(ticketId)).thenReturn(Optional.empty());
        final var command = ValidateTicketCommand.with("show-1", ticketId.getValue(), "CODE", "sig");

        final var notification = useCase.execute(command).getLeft();

        assertEquals(
                "Ticket not found: " + ticketId.getValue(),
                notification.firstError().message());
        verify(ticketGateway, times(1)).findById(ticketId);
        verify(spotGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given tampered code, when execute, then rejects signature")
    void givenTamperedCode_whenExecute_thenRejectsSignature() {
        final var spot = Spot.create(Location.create("A00001"));
        final var ticket = givenTicket(spot);
        final var command =
                ValidateTicketCommand.with("show-1", ticket.getId().getValue(), "TAMPERED", ticket.getSignature());

        final var notification = useCase.execute(command).getLeft();

        assertEquals("Invalid ticket signature", notification.firstError().message());
        verify(ticketGateway, times(1)).findById(ticket.getId());
        verify(ticketGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given used ticket, when execute, then rejects reuse")
    void givenUsedTicket_whenExecute_thenRejectsReuse() {
        final var spot = Spot.create(Location.create("A00001"));
        final var show = givenShow(TODAY);
        final var ticket = givenTicket(spot);
        givenPlacement(spot, show);
        useCase.execute(ValidateTicketCommand.with(
                show.getId().getValue(), ticket.getId().getValue(), ticket.getCode(), ticket.getSignature()));

        final var notification = useCase.execute(ValidateTicketCommand.with(
                        show.getId().getValue(), ticket.getId().getValue(),
                        ticket.getCode(), ticket.getSignature()))
                .getLeft();

        assertEquals("Ticket is already used", notification.firstError().message());
        verify(ticketGateway, times(2)).findById(ticket.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(ticketGateway, times(1)).update(ticket);
        verify(spotGateway, times(1)).update(spot);
    }

    @Test
    @DisplayName("Given ticket from another show, when execute, then rejects vínculo")
    void givenTicketFromAnotherShow_whenExecute_thenRejects() {
        final var spot = Spot.create(Location.create("A00001"));
        final var ticket = givenTicket(spot);
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, "show-2", "section-9")));
        final var command = ValidateTicketCommand.with(
                "show-1", ticket.getId().getValue(), ticket.getCode(), ticket.getSignature());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(
                "Spot does not belong to the given show and section",
                notification.firstError().message());
        verify(ticketGateway, times(1)).findById(ticket.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(0)).findById(any());
    }

    @Test
    @DisplayName("Given show on another date, when execute, then rejects check-in")
    void givenShowOnAnotherDate_whenExecute_thenRejects() {
        final var spot = Spot.create(Location.create("A00001"));
        final var show = givenShow(YESTERDAY);
        final var ticket = givenTicket(spot);
        givenPlacement(spot, show);
        final var command = ValidateTicketCommand.with(
                show.getId().getValue(), ticket.getId().getValue(), ticket.getCode(), ticket.getSignature());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(
                "Show is outside the check-in date (showDate=" + YESTERDAY + ")",
                notification.firstError().message());
        verify(ticketGateway, times(1)).findById(ticket.getId());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(ticketGateway, times(0)).update(any());
    }
}
