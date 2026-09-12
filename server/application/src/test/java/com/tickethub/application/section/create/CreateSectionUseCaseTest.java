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
import com.tickethub.domain.shared.Money;

public class CreateSectionUseCaseTest extends UseCaseTest {
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    @InjectMocks
    private DefaultCreateSectionUseCase useCase;

    @Mock
    private SectionGateway sectionGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(sectionGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateSectionCommand.with("VIP", "Description", 3, PRICE);
        when(sectionGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
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
                        && !saved.isPublished()));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateSectionCommand.with("VIP", "Description", 3, PRICE);
        final var expectedMessage = "Gateway error";
        when(sectionGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

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
                        && !saved.isPublished()));
    }

    @Test
    public void givenInvalidCommand_whenExecute_shouldReturnValidationErrorsWithoutPersisting() {
        final var command = CreateSectionCommand.with("VIP", "Description", 3, null);

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("'price' should not be null", notification.firstError().message());
        verify(sectionGateway, never()).create(any());
    }
}
