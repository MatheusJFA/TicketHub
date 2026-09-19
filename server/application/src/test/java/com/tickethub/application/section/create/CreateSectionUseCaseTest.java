package com.tickethub.application.section.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.shared.Money;

public class CreateSectionUseCaseTest extends UseCaseTest {
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
    private static final ShowID SHOW_ID = ShowID.generate();

    @InjectMocks
    private DefaultCreateSectionUseCase useCase;

    @Mock
    private SectionGateway sectionGateway;

    @Mock
    private ShowGateway showGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(sectionGateway, showGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));
        when(sectionGateway.create(any(), eq(SHOW_ID))).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(showGateway, times(1)).existsByIds(List.of(SHOW_ID));
        verify(sectionGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("VIP")
                        && saved.getDescription().getValue().equals("Description")
                        && saved.getTotalSpots() == 3
                        && saved.getSpots().size() == 3
                        && saved.getPrice().equals(PRICE)
                        && !saved.isPublished()), eq(SHOW_ID));
    }

    @Test
    public void givenMissingShow_whenExecute_shouldReturnNotFoundWithoutPersisting() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Show not found: " + SHOW_ID.getValue(), notification.firstError().message());
        verify(sectionGateway, never()).create(any(), any());
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, PRICE);
        final var expectedMessage = "Gateway error";
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));
        when(sectionGateway.create(any(), eq(SHOW_ID))).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(sectionGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getName().getValue().equals("VIP")
                        && saved.getDescription().getValue().equals("Description")
                        && saved.getTotalSpots() == 3
                        && saved.getSpots().size() == 3
                        && saved.getPrice().equals(PRICE)
                        && !saved.isPublished()), eq(SHOW_ID));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateSectionCommand.with(SHOW_ID.getValue(), "VIP", "Description", 3, null);
        when(showGateway.existsByIds(List.of(SHOW_ID))).thenReturn(List.of(SHOW_ID));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("'price' should not be null", notification.firstError().message());
        verify(sectionGateway, never()).create(any(), any());
    }
}
