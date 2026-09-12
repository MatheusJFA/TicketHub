package com.tickethub.application.show.changename;

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
import com.tickethub.domain.core.show.*;

class ChangeShowNameUseCaseTest {
    private final Show entity = Show.create("Show original", "Descricao original", OffsetDateTime.parse("2030-01-01T20:00:00Z"), Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), 10, PartnerID.generate());
    private final ShowGateway gateway = mock(ShowGateway.class);
    private final DefaultChangeShowNameUseCase useCase = new DefaultChangeShowNameUseCase(gateway);
    private final String id = entity.getId().getValue();
    private final String value = "Novo nome";

    @Test
    void changesOnlyRequestedFieldAndPersists() {
        final var createdAt = entity.getCreatedAt();
        final var updatedAt = entity.getUpdatedAt();
        final var originaldescription = entity.getDescription().getValue();
        final var originaldate = entity.getDate();
        final var originaladdress = entity.getAddress();
        final var originalpublished = entity.isPublished();
        final var originaltotalSpots = entity.getTotalSpots();
        final var originaltotalSpotsSold = entity.getTotalSpotsSold();
        final var originalpartnerId = entity.getPartnerId().getValue();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = ChangeShowNameCommand.with(id, value);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(value, entity.getName().getValue());
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(updatedAt));
        assertEquals(originaldescription, entity.getDescription().getValue());
        assertEquals(originaldate, entity.getDate());
        assertEquals(originaladdress, entity.getAddress());
        assertEquals(originalpublished, entity.isPublished());
        assertEquals(originaltotalSpots, entity.getTotalSpots());
        assertEquals(originaltotalSpotsSold, entity.getTotalSpotsSold());
        assertEquals(originalpartnerId, entity.getPartnerId().getValue());
        verify(gateway).update(entity);
    }

    @Test
    void rejectsInvalidValueWithoutMutationOrPersistence() {
        final var original = entity.getName();
        final var updatedAt = entity.getUpdatedAt();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = ChangeShowNameCommand.with(id, null);
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        assertEquals(original, entity.getName());
        assertEquals(updatedAt, entity.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = ChangeShowNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("Show not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsLookupFailureWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var command = ChangeShowNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("lookup failed", result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    void reportsPersistenceFailure() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenThrow(new IllegalStateException("save failed"));
        final var command = ChangeShowNameCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("save failed", result.getLeft().firstError().message());
    }
}
