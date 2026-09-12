package com.tickethub.application.section.changename;

import java.util.List;
import java.util.Optional;
import java.util.Currency;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.tickethub.domain.shared.*;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.core.section.*;

class ChangeSectionNameUseCaseTest {
    private final Section entity = Section.create("Setor original", "Descricao original", 2, Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL")));
    private final SectionGateway gateway = mock(SectionGateway.class);
    private final DefaultChangeSectionNameUseCase useCase = new DefaultChangeSectionNameUseCase(gateway);
    private final String id = entity.getId().getValue();
    private final String value = "Novo nome";

    @Test
    void changesOnlyRequestedFieldAndPersists() {
        final var createdAt = entity.getCreatedAt();
        final var updatedAt = entity.getUpdatedAt();
        final var originaldescription = entity.getDescription().getValue();
        final var originalprice = entity.getPrice();
        final var originalpublished = entity.isPublished();
        final var originaltotalSpots = entity.getTotalSpots();
        final var originaltotalSpotsSold = entity.getTotalSpotsSold();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = ChangeSectionNameCommand.with(id, value);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(value, entity.getName().getValue());
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(updatedAt));
        assertEquals(originaldescription, entity.getDescription().getValue());
        assertEquals(originalprice, entity.getPrice());
        assertEquals(originalpublished, entity.isPublished());
        assertEquals(originaltotalSpots, entity.getTotalSpots());
        assertEquals(originaltotalSpotsSold, entity.getTotalSpotsSold());
        verify(gateway).update(entity);
    }

    @Test
    void rejectsInvalidValueWithoutMutationOrPersistence() {
        final var original = entity.getName();
        final var updatedAt = entity.getUpdatedAt();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = ChangeSectionNameCommand.with(id, null);
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        assertEquals(original, entity.getName());
        assertEquals(updatedAt, entity.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = ChangeSectionNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("Section not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsLookupFailureWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var command = ChangeSectionNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("lookup failed", result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsPersistenceFailure() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenThrow(new IllegalStateException("save failed"));
        final var command = ChangeSectionNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("save failed", result.getLeft().firstError().message());
    }
}
