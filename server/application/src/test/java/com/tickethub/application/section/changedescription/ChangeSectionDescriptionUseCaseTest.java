package com.tickethub.application.section.changedescription;

import java.util.List;
import java.util.Optional;
import java.util.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.core.section.*;

@DisplayName("Change section description use case")
class ChangeSectionDescriptionUseCaseTest {
    private final Section entity = Section.create("Setor original", "Descricao original", 2, Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")), "A", 5);
    private final SectionGateway gateway = mock(SectionGateway.class);
    private final DefaultChangeSectionDescriptionUseCase useCase = new DefaultChangeSectionDescriptionUseCase(gateway);
    private final String id = entity.getId().getValue();
    private final String value = "Nova descricao";

    @Test
    @DisplayName("Changes only requested field and persists")
    void changesOnlyRequestedFieldAndPersists() {
        final var createdAt = entity.getCreatedAt();
        final var updatedAt = entity.getUpdatedAt();
        final var originalname = entity.getName().getValue();
        final var originalprice = entity.getPrice();
        final var originalpublished = entity.isPublished();
        final var originaltotalSpots = entity.getTotalSpots();
        final var originaltotalSpotsSold = entity.getTotalSpotsSold();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = ChangeSectionDescriptionCommand.with(id, value);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(value, entity.getDescription().getValue());
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(updatedAt));
        assertEquals(originalname, entity.getName().getValue());
        assertEquals(originalprice, entity.getPrice());
        assertEquals(originalpublished, entity.isPublished());
        assertEquals(originaltotalSpots, entity.getTotalSpots());
        assertEquals(originaltotalSpotsSold, entity.getTotalSpotsSold());
        verify(gateway).update(entity);
    }

    @Test
    @DisplayName("Rejects invalid value without mutation or persistence")
    void rejectsInvalidValueWithoutMutationOrPersistence() {
        final var original = entity.getDescription();
        final var updatedAt = entity.getUpdatedAt();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = ChangeSectionDescriptionCommand.with(id, "x".repeat(1001));
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        assertEquals(original, entity.getDescription());
        assertEquals(updatedAt, entity.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports missing entity without persistence")
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = ChangeSectionDescriptionCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("Section not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports lookup failure without persistence")
    void reportsLookupFailureWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var command = ChangeSectionDescriptionCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("lookup failed", result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports persistence failure")
    void reportsPersistenceFailure() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenThrow(new IllegalStateException("save failed"));
        final var command = ChangeSectionDescriptionCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("save failed", result.getLeft().firstError().message());
    }
}
