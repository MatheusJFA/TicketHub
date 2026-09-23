package com.tickethub.application.section.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.shared.Money;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Create section use case")
public class CreateSectionUseCaseTest extends UseCaseTest {
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
    private static final ShowID SHOW_ID = ShowID.generate();
    private static final int SEAT_NUMBER_WIDTH = 5;

    private final SectionGateway sectionGateway = mock(SectionGateway.class);
    private final ShowGateway showGateway = mock(ShowGateway.class);
    private final DefaultCreateSectionUseCase useCase =
            new DefaultCreateSectionUseCase(sectionGateway, showGateway, SEAT_NUMBER_WIDTH);

    @Override
    protected List<Object> getMocks() {
        return List.of(sectionGateway, showGateway);
    }

    @Test
    @DisplayName("Given valid command, when execute, should persist and return id")
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));
        when(sectionGateway.create(any(), eq(SHOW_ID))).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(showGateway, times(1)).existsByIds(List.of(SHOW_ID));
        verify(sectionGateway, times(1))
                .create(
                        argThat(saved -> saved.getId() != null
                                && saved.getId().getValue().equals(output.id())
                                && saved.getCreatedAt() != null
                                && saved.getUpdatedAt() != null
                                && saved.getDeletedAt() == null
                                && saved.getName().getValue().equals("VIP")
                                && saved.getDescription().getValue().equals("Description")
                                && saved.getTotalSpots() == 3
                                && saved.getSpots().size() == 3
                                && saved.getPrice().equals(PRICE)
                                && !saved.isPublished()),
                        eq(SHOW_ID));
    }

    @Test
    @DisplayName("Given missing show, when execute, should return not found without persisting")
    public void givenMissingShow_whenExecute_shouldReturnNotFoundWithoutPersisting() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(
                "Show not found: " + SHOW_ID.getValue(),
                notification.firstError().message());
        verify(showGateway, times(1)).existsByIds(List.of(SHOW_ID));
        verify(sectionGateway, never()).create(any(), any());
    }

    @Test
    @DisplayName("Given gateway failure, when execute, should return notification")
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        final var expectedMessage = "Gateway error";
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));
        when(sectionGateway.create(any(), eq(SHOW_ID))).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(showGateway, times(1)).existsByIds(List.of(SHOW_ID));
        verify(sectionGateway, times(1))
                .create(
                        argThat(saved -> saved.getId() != null
                                && saved.getCreatedAt() != null
                                && saved.getUpdatedAt() != null
                                && saved.getDeletedAt() == null
                                && saved.getName().getValue().equals("VIP")
                                && saved.getDescription().getValue().equals("Description")
                                && saved.getTotalSpots() == 3
                                && saved.getSpots().size() == 3
                                && saved.getPrice().equals(PRICE)
                                && !saved.isPublished()),
                        eq(SHOW_ID));
    }

    @Test
    @DisplayName("Given invalid command, when execute, should return validation errors without persisting")
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, null);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("'price' should not be null", notification.firstError().message());
        verify(showGateway, times(1)).existsByIds(List.of(SHOW_ID));
        verify(sectionGateway, never()).create(any(), any());
    }
}
