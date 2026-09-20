package com.tickethub.application.ticket.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.core.spot.SpotPlacement;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Location;

@DisplayName("Validate ticket use case")
class ValidateTicketUseCaseTest extends UseCaseTest {

    private static final Instant NOW = Instant.parse("2027-01-15T22:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final OffsetDateTime TODAY = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final OffsetDateTime YESTERDAY = OffsetDateTime.parse("2027-01-14T20:00:00-03:00");
    private static final Address ADDRESS = Address.create("Rua Augusta", "100", null, "Centro",
            "São Paulo", "SP", "Brasil", "01305-000");

    private final SpotGateway spotGateway = org.mockito.Mockito.mock(SpotGateway.class);
    private final ShowGateway showGateway = org.mockito.Mockito.mock(ShowGateway.class);
    private final DefaultValidateTicketUseCase useCase =
            new DefaultValidateTicketUseCase(spotGateway, showGateway, CLOCK);

    @Override
    protected List<Object> getMocks() {
        return List.of(spotGateway, showGateway);
    }

    @Test
    @DisplayName("Given valid QR data for today show, when execute, then checks spot in")
    void givenValidQrData_whenExecute_thenChecksSpotIn() {
        final var spot = Spot.create(Location.create("A00001"));
        final var show = Show.create("Concert", "Description", TODAY, ADDRESS, 10,
                PartnerID.generate());
        final var command = ValidateTicketCommand.with(show.getId().getValue(), "section-1",
                spot.getId().getValue());
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, show.getId().getValue(), "section-1")));
        when(showGateway.findById(show.getId())).thenReturn(Optional.of(show));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output);
        assertEquals(spot.getId().getValue(), output.spotId());
        assertEquals(show.getId().getValue(), output.showId());
        assertEquals("section-1", output.sectionId());
        assertFalse(spot.isAvailable(),
                () -> "Spot should be marked as used after check-in");
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(spotGateway, times(1)).update(spot);
    }

    @Test
    @DisplayName("Given unknown spot, when execute, then returns not found")
    void givenUnknownSpot_whenExecute_thenReturnsNotFound() {
        final var spot = Spot.create(Location.create("A00001"));
        final var command = ValidateTicketCommand.with("show-1", "section-1",
                spot.getId().getValue());
        when(spotGateway.findPlacement(spot.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals("Spot not found: " + spot.getId().getValue(),
                notification.firstError().message());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(0)).findById(any());
        verify(spotGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given spot from another show, when execute, then rejects vínculo")
    void givenSpotFromAnotherShow_whenExecute_thenRejects() {
        final var spot = Spot.create(Location.create("A00001"));
        final var command = ValidateTicketCommand.with("show-1", "section-1",
                spot.getId().getValue());
        when(spotGateway.findPlacement(spot.getId())).thenReturn(
                Optional.of(new SpotPlacement(spot, "show-2", "section-9")));

        final var notification = useCase.execute(command).getLeft();

        assertEquals("Spot does not belong to the given show and section",
                notification.firstError().message());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(0)).findById(any());
        verify(spotGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given show outside check-in date, when execute, then rejects with date error")
    void givenShowOutsideDate_whenExecute_thenRejects() {
        final var spot = Spot.create(Location.create("A00001"));
        final var show = Show.create("Concert", "Description", YESTERDAY, ADDRESS, 10,
                PartnerID.generate());
        final var command = ValidateTicketCommand.with(show.getId().getValue(), "section-1",
                spot.getId().getValue());
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, show.getId().getValue(), "section-1")));
        when(showGateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var notification = useCase.execute(command).getLeft();

        assertEquals("Show is outside the check-in date", notification.firstError().message());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(spotGateway, times(0)).update(any());
    }

    @Test
    @DisplayName("Given already used spot, when execute, then rejects reuse")
    void givenAlreadyUsedSpot_whenExecute_thenRejects() {
        final var spot = Spot.create(Location.create("A00001"));
        spot.checkIn();
        final var show = Show.create("Concert", "Description", TODAY, ADDRESS, 10,
                PartnerID.generate());
        final var command = ValidateTicketCommand.with(show.getId().getValue(), "section-1",
                spot.getId().getValue());
        when(spotGateway.findPlacement(spot.getId()))
                .thenReturn(Optional.of(new SpotPlacement(spot, show.getId().getValue(), "section-1")));
        when(showGateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var notification = useCase.execute(command).getLeft();

        assertEquals("Spot is already used", notification.firstError().message());
        verify(spotGateway, times(1)).findPlacement(spot.getId());
        verify(showGateway, times(1)).findById(show.getId());
        verify(spotGateway, times(0)).update(any());
    }
}
