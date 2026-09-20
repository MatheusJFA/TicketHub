package com.tickethub.application.show.addsection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Money;

@DisplayName("Add section to show use case")
public class AddSectionToShowUseCaseTest extends UseCaseTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Address ADDRESS = Address.create("Rua Augusta", "100", null, "Centro", "São Paulo", "SP", "Brasil", "01305-000");
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    private static final long ASYNC_THRESHOLD = 1000;

    private DefaultAddSectionToShowUseCase useCase;

    @Mock
    private ShowGateway showGateway;

    @Mock
    private DomainEventPublisher eventPublisher;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        useCase = new DefaultAddSectionToShowUseCase(showGateway, eventPublisher, ASYNC_THRESHOLD);
    }

    @Override
    protected List<Object> getMocks() {
        return List.of(showGateway, eventPublisher);
    }

    @Test
    @DisplayName("Given valid command, when execute, should persist and return id")
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "General", "Description", 3, PRICE);
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
                        && saved.getTotalSpots() == 5
                        && saved.getSections().size() == 2
                        && saved.getSections().stream().anyMatch(section -> section.getName().getValue().equals("General")
                        && section.getSpots().size() == 3
                        && section.getPrice().equals(PRICE))));
    }

    @Test
    @DisplayName("Given gateway failure, when execute, should return notification")
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "General", "Description", 3, PRICE);
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
                        && saved.getTotalSpots() == 5
                        && saved.getSections().size() == 2
                        && saved.getSections().stream().anyMatch(section -> section.getName().getValue().equals("General")
                        && section.getSpots().size() == 3
                        && section.getPrice().equals(PRICE))));
    }

    @Test
    @DisplayName("Given missing show, when execute, should not persist")
    public void givenMissingShow_whenExecute_shouldNotPersist() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "General", "Description", 3, PRICE);
        when(showGateway.findById(entity.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Show not found: " + entity.getId().getValue(), notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, never()).update(any());
    }

    @Test
    @DisplayName("Given lookup failure, when execute, should return notification")
    public void givenLookupFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "General", "Description", 3, PRICE);
        when(showGateway.findById(entity.getId())).thenThrow(new IllegalStateException("Lookup failed"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Lookup failed", notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, never()).update(any());
    }

    @Test
    @DisplayName("Given large section, when execute, should persist shell and publish event")
    public void givenLargeSection_whenExecute_shouldPersistShellAndPublishEvent() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "Arena", "Description", 1500, PRICE);
        when(showGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(showGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertEquals(entity.getId().getValue(), output.id());
        verify(showGateway, times(1)).update(argThat(saved ->
                saved.getSections().size() == 2
                        && saved.getTotalSpots() == 1502
                        && saved.getSections().stream()
                                .filter(section -> section.getName().getValue().equals("Arena"))
                                .allMatch(section -> section.getSpots().isEmpty()
                                        && section.getTotalSpots() == 1500)));
        verify(eventPublisher, times(1)).publish(argThat(event ->
                event instanceof SpotsGenerationRequested requested
                        && requested.showId().equals(entity.getId().getValue())
                        && requested.sectionCode().equals("B")
                        && requested.totalSpots() == 1500
                        && requested.occurredOn() != null));
    }

    @Test
    @DisplayName("Given publish failure, when execute, should degrade to bulk append")
    public void givenPublishFailure_whenExecute_shouldDegradeToBulkAppend() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "Arena", "Description", 1500, PRICE);
        when(showGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(showGateway.update(any())).thenAnswer(returnsFirstArg());
        doThrow(new IllegalStateException("kafka down")).when(eventPublisher).publish(any());

        final var output = useCase.execute(command).getRight();

        assertEquals(entity.getId().getValue(), output.id());
        verify(showGateway, times(1)).update(any());
        verify(eventPublisher, times(1)).publish(any());
        verify(showGateway, times(1)).appendSpots(eq(entity.getId()), any(),
                argThat(spots -> spots.size() == 1500
                        && spots.stream().allMatch(spot -> spot.getLocation().getValue().matches("B\\d{5}"))));
    }

    private static Show entity() {
        final var entity = Show.create("Concert", "Description", DATE, ADDRESS, 0, PartnerID.generate());
        entity.addSection("VIP", "Description", 2, PRICE);
        return entity;
    }

    @Test
    @DisplayName("Given invalid command, when execute, should return validation errors without persisting")
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var entity = entity();
        final var command = AddSectionToShowCommand.with(entity.getId().getValue(), "General", "Description", 3, null);
        when(showGateway.findById(entity.getId())).thenReturn(Optional.of(entity));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("'price' should not be null", notification.firstError().message());
        verify(showGateway, times(1)).findById(entity.getId());
        verify(showGateway, never()).update(any());
        assertEquals(2, entity.getTotalSpots());
        assertEquals(1, entity.getSections().size());
    }
}
