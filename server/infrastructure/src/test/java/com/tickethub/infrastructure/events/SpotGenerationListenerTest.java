package com.tickethub.infrastructure.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tickethub.application.Either;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsOutput;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsUseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

import tools.jackson.databind.ObjectMapper;

@DisplayName("SpotGenerationListener")
class SpotGenerationListenerTest {

    private final GenerateSectionSpotsUseCase useCase = mock(GenerateSectionSpotsUseCase.class);
    private final SpotGenerationListener listener = new SpotGenerationListener(useCase, new ObjectMapper());

    private static String payload() {
        return """
                {"type":"SpotsGenerationRequested","showId":"show-1",\
                "sectionId":"section-1","sectionCode":"B","totalSpots":1500}\
                """;
    }

    @Test
    @DisplayName("Given spots requested, when message, then generates section spots")
    void givenSpotsRequested_whenMessage_thenGeneratesSectionSpots() {
        when(useCase.execute(any()))
                .thenReturn(Either.right(GenerateSectionSpotsOutput.from("section-1", 1500)));

        listener.onMessage(payload());

        verify(useCase).execute(argThat(command ->
                command.showId().equals("show-1")
                        && command.sectionId().equals("section-1")
                        && command.sectionCode().equals("B")));
    }

    @Test
    @DisplayName("Given use case failure, when message, then throws for retry")
    void givenUseCaseFailure_whenMessage_thenThrowsForRetry() {
        when(useCase.execute(any()))
                .thenReturn(Either.left(Notification.create(new Error("mongo down"))));

        final var exception = assertThrows(IllegalStateException.class, () -> listener.onMessage(payload()),
                () -> "Failing use case should throw IllegalStateException for retry");

        assertEquals("Spot generation failed: mongo down", exception.getMessage(),
                () -> "Exception message should include the use case failure detail");
    }

    @Test
    @DisplayName("Given unknown type, when message, then ignores")
    void givenUnknownType_whenMessage_thenIgnores() {
        listener.onMessage("""
                {"type":"SomethingElse","showId":"show-1",\
                "sectionId":"section-1","sectionCode":"B","totalSpots":1500}\
                """);

        verify(useCase, never()).execute(any());
    }

    @Test
    @DisplayName("Given malformed payload, when message, then throws")
    void givenMalformedPayload_whenMessage_thenThrows() {
        final var exception = assertThrows(IllegalStateException.class, () -> listener.onMessage("not-json"),
                () -> "Malformed payload should throw IllegalStateException");

        assertEquals("Invalid spot generation message", exception.getMessage(),
                () -> "Exception message should indicate the invalid spot generation message");
    }
}
