package com.tickethub.application.spot.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.shared.Location;

public class CreateSpotUseCaseTest extends UseCaseTest {

    @InjectMocks
    private DefaultCreateSpotUseCase useCase;

    @Mock
    private SpotGateway spotGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(spotGateway);
    }

    @Test
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateSpotCommand.with(Location.create("A1"));
        when(spotGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(spotGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getLocation().equals(Location.create("A1"))
                        && saved.isAvailable()
                        && !saved.isPublished()));
    }

    @Test
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateSpotCommand.with(Location.create("A1"));
        final var expectedMessage = "Gateway error";
        when(spotGateway.create(any())).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(spotGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getLocation().equals(Location.create("A1"))
                        && saved.isAvailable()
                        && !saved.isPublished()));
    }

    @Test
    public void givenNoLocation_whenExecute_shouldCreateAvailableUnpublishedSpot() {
        final var command = CreateSpotCommand.with(null);
        when(spotGateway.create(any())).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(spotGateway, times(1)).create(argThat(spot ->
                spot.getLocation() != null
                        && spot.getLocation().getValue().matches("[A-Z]\\d{5}")
                        && spot.isAvailable()
                        && !spot.isPublished()));
    }
}
