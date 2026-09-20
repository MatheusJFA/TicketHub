package com.tickethub.application.spot.create;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.tickethub.application.UseCaseTest;
import com.tickethub.domain.core.section.SectionGateway;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.SpotGateway;
import com.tickethub.domain.shared.Location;

@DisplayName("Create spot use case")
public class CreateSpotUseCaseTest extends UseCaseTest {

    private static final SectionID SECTION_ID = SectionID.generate();

    @InjectMocks
    private DefaultCreateSpotUseCase useCase;

    @Mock
    private SpotGateway spotGateway;

    @Mock
    private SectionGateway sectionGateway;

    @Override
    protected List<Object> getMocks() {
        return List.of(spotGateway, sectionGateway);
    }

    @Test
    @DisplayName("Given valid command, when execute, should persist and return id")
    public void givenValidCommand_whenExecute_shouldPersistAndReturnId() {
        final var command = CreateSpotCommand.with(SECTION_ID.getValue(), Location.create("A1"));
        when(sectionGateway.existsByIds(List.of(SECTION_ID))).thenReturn(List.of(SECTION_ID));
        when(spotGateway.create(any(), eq(SECTION_ID))).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(sectionGateway, times(1)).existsByIds(List.of(SECTION_ID));
        verify(spotGateway, times(1)).create(argThat(saved ->
                saved.getId() != null
                        && saved.getId().getValue().equals(output.id())
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getLocation().equals(Location.create("A1"))
                        && saved.isAvailable()
                        && !saved.isPublished()), eq(SECTION_ID));
    }

    @Test
    @DisplayName("Given missing section, when execute, should return not found without persisting")
    public void givenMissingSection_whenExecute_shouldReturnNotFoundWithoutPersisting() {
        final var command = CreateSpotCommand.with(SECTION_ID.getValue(), Location.create("A1"));
        when(sectionGateway.existsByIds(List.of(SECTION_ID))).thenReturn(List.of());

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Section not found: " + SECTION_ID.getValue(), notification.firstError().message());
        verify(spotGateway, never()).create(any(), any());
    }

    @Test
    @DisplayName("Given gateway failure, when execute, should return notification")
    public void givenGatewayFailure_whenExecute_shouldReturnNotification() {
        final var command = CreateSpotCommand.with(SECTION_ID.getValue(), Location.create("A1"));
        final var expectedMessage = "Gateway error";
        when(sectionGateway.existsByIds(List.of(SECTION_ID))).thenReturn(List.of(SECTION_ID));
        when(spotGateway.create(any(), eq(SECTION_ID))).thenThrow(new IllegalStateException(expectedMessage));

        final var notification = useCase.execute(command).getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals(expectedMessage, notification.firstError().message());
        verify(spotGateway, times(1)).create(argThat(saved -> saved.getId() != null
                        && saved.getCreatedAt() != null
                        && saved.getUpdatedAt() != null
                        && saved.getDeletedAt() == null
                        && saved.getLocation().equals(Location.create("A1"))
                        && saved.isAvailable()
                        && !saved.isPublished()), eq(SECTION_ID));
    }

    @Test
    @DisplayName("Given no location, when execute, should create available unpublished spot")
    public void givenNoLocation_whenExecute_shouldCreateAvailableUnpublishedSpot() {
        final var command = CreateSpotCommand.with(SECTION_ID.getValue(), null);
        when(sectionGateway.existsByIds(List.of(SECTION_ID))).thenReturn(List.of(SECTION_ID));
        when(spotGateway.create(any(), eq(SECTION_ID))).thenAnswer(returnsFirstArg());

        final var output = useCase.execute(command).getRight();

        assertNotNull(output.id());
        verify(spotGateway, times(1)).create(argThat(spot ->
                spot.getLocation() != null
                        && spot.getLocation().getValue().matches("[A-Z]\\d{5}")
                        && spot.isAvailable()
                        && !spot.isPublished()), eq(SECTION_ID));
    }
}
