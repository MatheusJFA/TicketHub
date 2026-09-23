package com.tickethub.infrastructure.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.event.DomainEvent;
import com.tickethub.infrastructure.exception.EventPublishException;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

@DisplayName("KafkaDomainEventPublisher")
class KafkaDomainEventPublisherTest {

    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaDomainEventPublisher publisher =
            new KafkaDomainEventPublisher(kafkaTemplate, new ObjectMapper());

    @Test
    @DisplayName("Given spots requested, when publish, then sends keyed message")
    void givenSpotsRequested_whenPublish_thenSendsKeyedMessage() {
        final var event = new SpotsGenerationRequested("show-1", "section-1", "B", 1500, Instant.now());

        publisher.publish(event);

        verify(kafkaTemplate)
                .sendDefault(
                        eq("show-1"),
                        argThat(payload -> payload.contains("\"type\":\"SpotsGenerationRequested\"")
                                && payload.contains("\"showId\":\"show-1\"")
                                && payload.contains("\"sectionId\":\"section-1\"")
                                && payload.contains("\"sectionCode\":\"B\"")
                                && payload.contains("\"totalSpots\":1500")));
    }

    @Test
    @DisplayName("Given unsupported event, when publish, then ignores")
    void givenUnsupportedEvent_whenPublish_thenIgnores() {
        final DomainEvent event = () -> Instant.now();

        publisher.publish(event);

        verify(kafkaTemplate, never()).sendDefault(any(), any());
    }

    @Test
    @DisplayName("Given serialization failure, when publish, then throws")
    void givenSerializationFailure_whenPublish_thenThrows() throws Exception {
        final var event = new SpotsGenerationRequested("show-1", "section-1", "B", 1500, Instant.now());
        final var failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new IllegalStateException("mapper down"));
        final var failing = new KafkaDomainEventPublisher(kafkaTemplate, failingMapper);

        final var exception = assertThrows(
                EventPublishException.class,
                () -> failing.publish(event),
                () -> "Publishing with a failing mapper should throw EventPublishException");

        assertEquals(
                "Failed to publish spot generation event",
                exception.getMessage(),
                () -> "Exception message should indicate the spot generation publish failure");
        assertNotNull(exception.getCause(), () -> "Exception should preserve the mapper failure as cause");
        assertEquals(
                "mapper down",
                exception.getCause().getMessage(),
                () -> "Exception cause should preserve the mapper failure message");
    }
}
