package com.tickethub.infrastructure.events;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.tickethub.application.Either;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsOutput;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsUseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

import tools.jackson.databind.ObjectMapper;

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
    void givenUseCaseFailure_whenMessage_thenThrowsForRetry() {
        when(useCase.execute(any()))
                .thenReturn(Either.left(Notification.create(new Error("mongo down"))));

        assertThrows(IllegalStateException.class, () -> listener.onMessage(payload()));
    }

    @Test
    void givenUnknownType_whenMessage_thenIgnores() {
        listener.onMessage("""
                {"type":"SomethingElse","showId":"show-1",\
                "sectionId":"section-1","sectionCode":"B","totalSpots":1500}\
                """);

        verify(useCase, never()).execute(any());
    }

    @Test
    void givenMalformedPayload_whenMessage_thenThrows() {
        assertThrows(IllegalStateException.class, () -> listener.onMessage("not-json"));
    }
}
