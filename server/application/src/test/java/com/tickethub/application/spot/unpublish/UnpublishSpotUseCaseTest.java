package com.tickethub.application.spot.unpublish;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotGateway;

@DisplayName("Unpublish spot use case")
public class UnpublishSpotUseCaseTest extends UseCaseTest {

    @InjectMocks
    private DefaultUnpublishSpotUseCase useCase;

    @Mock
    private SpotGateway spotGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(spotGateway);
    }

    @Test
    @DisplayName("Given valid command, when execute, should persist and return id")
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var entity = entity();
        final var command = UnpublishSpotCommand.with(entity.getId().getValue());
        when(spotGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(spotGateway.update(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        assertEquals(entity.getId().getValue(), output.id());
        verify(spotGateway, times(1)).findById(entity.getId());
        verify(spotGateway, times(1)).update(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()));
    }

    @Test
    @DisplayName("Given gateway failure, when execute, should return notification")
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishSpotCommand.with(entity.getId().getValue());
        when(spotGateway.findById(entity.getId())).thenReturn(Optional.of(entity));
        final var expectedMessage = "Gateway error";
        when(spotGateway.update(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(spotGateway, times(1)).findById(entity.getId());
        verify(spotGateway, times(1)).update(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && !saved.isPublished()));
    }

    @Test
    @DisplayName("Given missing spot, when execute, should not persist")
    public void givenMissingSpot_whenExecute_shouldNotPersist() {
        final var entity = entity();
        final var command = UnpublishSpotCommand.with(entity.getId().getValue());
        when(spotGateway.findById(entity.getId())).thenReturn(Optional.empty());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Spot not found: " + entity.getId().getValue(), notification.firstError().message());
        verify(spotGateway, times(1)).findById(entity.getId());
        verify(spotGateway, never()).update(any());
    }

    @Test
    @DisplayName("Given lookup failure, when execute, should return notification")
    public void givenLookupFailure_whenExecute_shouldReturnNotification() {
        final var entity = entity();
        final var command = UnpublishSpotCommand.with(entity.getId().getValue());
        when(spotGateway.findById(entity.getId())).thenThrow(new IllegalStateException("Lookup failed"));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Lookup failed", notification.firstError().message());
        verify(spotGateway, times(1)).findById(entity.getId());
        verify(spotGateway, never()).update(any());
    }

    private static Spot entity() {
        final var entity = Spot.create();
        entity.publish();
        return entity;
    }
}
