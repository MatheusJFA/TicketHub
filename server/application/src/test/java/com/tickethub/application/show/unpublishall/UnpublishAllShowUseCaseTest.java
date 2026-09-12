package com.tickethub.application.show.unpublishall;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Money;

public class UnpublishAllShowUseCaseTest extends UseCaseTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Address ADDRESS = Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000");
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    @InjectMocks
    private DefaultUnpublishAllShowUseCase useCase;

    @Mock
    private ShowGateway showGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(showGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var entity = entity();
        final var command = UnpublishAllShowCommand.with(entity.getId().getValue());
        when(showGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(showGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        assertEquals(entity.getId().getValue(), output.id());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, times(1)).update(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()
                        && saved.getSections().stream().allMatch(section -> !section.isPublished()
                        && section.getSpots().stream().allMatch(spot -> !spot.isPublished()))));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishAllShowCommand.with(entity.getId().getValue());
        when(showGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var expectedMessage = "Gateway error";
        when(showGateway.update(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, times(1)).update(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()
                        && saved.getSections().stream().allMatch(section -> !section.isPublished()
                        && section.getSpots().stream().allMatch(spot -> !spot.isPublished()))));
    }

    @Test
    public void givenMissingShow_whenExecute_shouldNotPersist() {
        final var entity = entity();
        final var command = UnpublishAllShowCommand.with(entity.getId().getValue());
        when(showGateway.findById(entity.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Show not found: " + entity.getId().getValue(), notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, never()).update(any());
    }

    @Test
    public void givenLookupFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishAllShowCommand.with(entity.getId().getValue());
        when(showGateway.findById(entity.getId())).thenThrow(new IllegalStateException("Lookup failed"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Lookup failed", notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, never()).update(any());
    }

    private static Show entity() {
        final var entity = Show.create("Concert", "Description", DATE, ADDRESS, 0, PartnerID.generate());
        entity.addSection("VIP", "Description", 2, PRICE);
        entity.publishAll();
        return entity;
    }
}
