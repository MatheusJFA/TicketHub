package com.tickethub.application.spot.update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tickethub.domain.core.spot.*;
import com.tickethub.domain.shared.*;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Update spot use case")
class UpdateSpotUseCaseTest {
    private final Spot entity = Spot.create(Location.create("A1"));
    private final SpotGateway gateway = mock(SpotGateway.class);
    private final DefaultUpdateSpotUseCase useCase = new DefaultUpdateSpotUseCase(gateway);
    private final String id = entity.getId().getValue();

    @Test
    @DisplayName("Updates location and persists")
    void updatesLocationAndPersists() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(gateway.update(entity)).thenReturn(entity);
        final var command = UpdateSpotCommand.with(id, Location.create("B2"));
        final var output = useCase.execute(command).getRight();
        assertEquals(id, output.id());
        assertEquals(Location.create("B2"), entity.getLocation());
        verify(gateway).update(entity);
    }

    @Test
    @DisplayName("Reports missing entity without persistence")
    void reportsMissingEntityWithoutPersistence() {
        when(gateway.findById(entity.getId())).thenReturn(Optional.empty());
        final var command = UpdateSpotCommand.with(id, Location.create("B2"));
        final var result = useCase.execute(command);
        assertEquals("Spot not found: " + id, result.getLeft().firstError().message());
        verify(gateway, never()).update(any());
    }
}
