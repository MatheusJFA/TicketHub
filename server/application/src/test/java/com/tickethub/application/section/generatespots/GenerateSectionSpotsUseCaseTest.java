package com.tickethub.application.section.generatespots;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Money;

@DisplayName("Generate section spots use case")
class GenerateSectionSpotsUseCaseTest {

    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Address ADDRESS = Address.create("Rua Augusta", "100", null, "Centro",
            "São Paulo", "SP", "Brasil", "01305-000");
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    private final ShowGateway gateway = mock(ShowGateway.class);
    private final DefaultGenerateSectionSpotsUseCase useCase = new DefaultGenerateSectionSpotsUseCase(gateway);

    private static Show entity() {
        final var show = Show.create("Concert", "Description", DATE, ADDRESS, 0, PartnerID.generate());
        show.addSection("VIP", "Description", 2, PRICE);
        return show;
    }

    @Test
    @DisplayName("Given missing show, when execute, should not persist")
    void givenMissingShow_whenExecute_shouldNotPersist() {
        when(gateway.findById(any())).thenReturn(Optional.empty());

        final var result = useCase.execute(GenerateSectionSpotsCommand.with("show-1", "section-1", "A"));

        assertEquals("Show not found: show-1", result.getLeft().firstError().message());
        verify(gateway, never()).appendSpots(any(), any(), any());
    }

    @Test
    @DisplayName("Given missing section, when execute, should not persist")
    void givenMissingSection_whenExecute_shouldNotPersist() {
        final var show = entity();
        when(gateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var result = useCase.execute(
                GenerateSectionSpotsCommand.with(show.getId().getValue(), "missing", "B"));

        assertEquals("Section not found: missing", result.getLeft().firstError().message());
        verify(gateway, never()).appendSpots(any(), any(), any());
    }

    @Test
    @DisplayName("Given shell section, when execute, should bulk append spots")
    void givenShellSection_whenExecute_shouldBulkAppendSpots() {
        final var show = entity();
        final var shell = show.addSectionShell("Arena", "Big", 3, PRICE);
        when(gateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var output = useCase.execute(GenerateSectionSpotsCommand.with(
                show.getId().getValue(), shell.getId().getValue(), "B")).getRight();

        assertEquals(shell.getId().getValue(), output.sectionId());
        assertEquals(3, output.generatedSpots());
        verify(gateway).appendSpots(any(), any(), any());
        verify(gateway, never()).update(any());
        final var codes = show.getSections().stream()
                .filter(section -> section.getId().equals(shell.getId()))
                .flatMap(section -> section.getSpots().stream())
                .map(spot -> spot.getLocation().getValue())
                .sorted()
                .toList();
        assertEquals(List.of("B00001", "B00002", "B00003"), codes);
    }

    @Test
    @DisplayName("Given complete section, when execute, should skip persistence")
    void givenCompleteSection_whenExecute_shouldSkipPersistence() {
        final var show = entity();
        final var section = show.getSections().iterator().next();
        when(gateway.findById(show.getId())).thenReturn(Optional.of(show));

        final var output = useCase.execute(GenerateSectionSpotsCommand.with(
                show.getId().getValue(), section.getId().getValue(), "A")).getRight();

        assertEquals(0, output.generatedSpots());
        verify(gateway, never()).appendSpots(any(), any(), any());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Given gateway failure, when execute, should return notification")
    void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var show = entity();
        final var shell = show.addSectionShell("Arena", "Big", 3, PRICE);
        when(gateway.findById(show.getId())).thenReturn(Optional.of(show));
        doThrow(new IllegalStateException("mongo down")).when(gateway).appendSpots(any(), any(), any());

        final var result = useCase.execute(GenerateSectionSpotsCommand.with(
                show.getId().getValue(), shell.getId().getValue(), "B"));

        assertTrue(result.isLeft());
        assertEquals("mongo down", result.getLeft().firstError().message());
    }
}
