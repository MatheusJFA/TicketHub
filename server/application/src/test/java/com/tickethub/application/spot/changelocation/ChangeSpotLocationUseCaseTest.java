package com.tickethub.application.spot.changelocation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.domain.core.spot.*;
import com.tickethub.domain.pagination.*;
import com.tickethub.domain.shared.*;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Change spot location use case")
class ChangeSpotLocationUseCaseTest {
    private final Spot entity = Spot.create(Location.create("A1"));
    private final SpotGateway gateway = mock(SpotGateway.class);
    private final DefaultChangeSpotLocationUseCase useCase = new DefaultChangeSpotLocationUseCase(gateway);
    private final String id = entity.getId().getValue();
    private final Location value = Location.create("B2");

    @Test
    @DisplayName("Changes only requested field and persists")
    void changesOnlyRequestedFieldAndPersists() {
        final var createdAt = entity.getCreatedAt();
        final var updatedAt = entity.getUpdatedAt();
        final var originalavailable = entity.isAvailable();
        final var originalpublished = entity.isPublished();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = ChangeSpotLocationCommand.with(id, value);
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(value, entity.getLocation());
        assertEquals(createdAt, entity.getCreatedAt());
        assertFalse(entity.getUpdatedAt().isBefore(updatedAt));
        assertEquals(originalavailable, entity.isAvailable());
        assertEquals(originalpublished, entity.isPublished());
        verify(gateway).update(entity);
    }

    @Test
    @DisplayName("Rejects invalid value without mutation or persistence")
    void rejectsInvalidValueWithoutMutationOrPersistence() {
        final var original = entity.getLocation();
        final var updatedAt = entity.getUpdatedAt();
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var command = ChangeSpotLocationCommand.with(id, null);
        final var notification = useCase.execute(command).getLeft();
        assertTrue(notification.hasError());
        assertEquals(original, entity.getLocation());
        assertEquals(updatedAt, entity.getUpdatedAt());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports missing entity without persistence")
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = ChangeSpotLocationCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("Spot not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports lookup failure without persistence")
    void reportsLookupFailureWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenThrow(new IllegalStateException("lookup failed"));
        final var command = ChangeSpotLocationCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("lookup failed", result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }

    @Test
    @DisplayName("Reports persistence failure")
    void reportsPersistenceFailure() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenThrow(new IllegalStateException("save failed"));
        final var command = ChangeSpotLocationCommand.with(id, value);
        final var result = useCase.execute(command);
        assertEquals("save failed", result.getLeft().firstError().message());
    }
}
