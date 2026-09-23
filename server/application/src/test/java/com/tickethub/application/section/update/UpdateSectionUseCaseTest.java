package com.tickethub.application.section.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.domain.core.section.*;
import com.tickethub.domain.shared.*;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Update section use case")
class UpdateSectionUseCaseTest {
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
    private static final Money NEW_PRICE = Money.create(new BigDecimal("99.90"), Currency.getInstance("BRL"));
    private final Section entity = Section.create("VIP", "Front stage", 2, PRICE, "A", 5);
    private final SectionGateway gateway = mock(SectionGateway.class);
    private final DefaultUpdateSectionUseCase useCase = new DefaultUpdateSectionUseCase(gateway);
    private final String id = entity.getId().getValue();

    @Test
    @DisplayName("Updates all fields and persists")
    void updatesAllFieldsAndPersists() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = UpdateSectionCommand.with(id, "Pista", "Geral", NEW_PRICE);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals("Pista", entity.getName().getValue());
        assertEquals("Geral", entity.getDescription().getValue());
        assertEquals(NEW_PRICE, entity.getPrice());
        verify(gateway).update(entity);
    }

    @Test
    @DisplayName("Rejects invalid value without persistence")
    void rejectsInvalidValueWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = UpdateSectionCommand.with(id, "Pista", "Geral", null);
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports missing entity without persistence")
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = UpdateSectionCommand.with(id, "Pista", "Geral", NEW_PRICE);
        final var result = useCase.execute(command);
        assertEquals("Section not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }
}
