package com.tickethub.application.section.unpublish;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.shared.Money;

public class UnpublishSectionUseCaseTest extends UseCaseTest {
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    @InjectMocks
    private DefaultUnpublishSectionUseCase useCase;

    @Mock
    private SectionGateway sectionGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(sectionGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var entity = entity();
        final var command = UnpublishSectionCommand.with(entity.getId().getValue());
        when(sectionGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(sectionGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        assertEquals(entity.getId().getValue(), output.id());
        verify(sectionGateway, times(1)).findById(entity.getId());
        verify(sectionGateway, times(1)).update(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()
                        && saved.getSpots().stream().allMatch(spot -> spot.isPublished())));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishSectionCommand.with(entity.getId().getValue());
        when(sectionGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var expectedMessage = "Gateway error";
        when(sectionGateway.update(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(sectionGateway, times(1)).findById(entity.getId());
        verify(sectionGateway, times(1)).update(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()
                        && saved.getSpots().stream().allMatch(spot -> spot.isPublished())));
    }

    @Test
    public void givenMissingSection_whenExecute_shouldNotPersist() {
        final var entity = entity();
        final var command = UnpublishSectionCommand.with(entity.getId().getValue());
        when(sectionGateway.findById(entity.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Section not found: " + entity.getId().getValue(), notification.firstError().message());
        verify(sectionGateway, times(1)).findById(entity.getId());
        verify(sectionGateway, never()).update(any());
    }

    @Test
    public void givenLookupFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishSectionCommand.with(entity.getId().getValue());
        when(sectionGateway.findById(entity.getId())).thenThrow(new IllegalStateException("Lookup failed"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Lookup failed", notification.firstError().message());
        verify(sectionGateway, times(1)).findById(entity.getId());
        verify(sectionGateway, never()).update(any());
    }

    private static Section entity() {
        final var entity = Section.create("VIP", "Description", 2, PRICE);
        entity.publishAll();
        return entity;
    }
}
