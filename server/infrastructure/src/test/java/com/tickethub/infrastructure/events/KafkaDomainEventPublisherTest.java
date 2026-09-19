package com.tickethub.infrastructure.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.event.DomainEvent;

import tools.jackson.databind.ObjectMapper;

class KafkaDomainEventPublisherTest {

    private final KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
    private final KafkaDomainEventPublisher publisher =
            new KafkaDomainEventPublisher(kafkaTemplate, new ObjectMapper());

    @Test
    void givenSpotsRequested_whenPublish_thenSendsKeyedMessage() {
        final var event = new SpotsGenerationRequested("show-1", "section-1", "B", 1500, Instant.now());

        publisher.publish(event);

        verify(kafkaTemplate).sendDefault(eq("show-1"), argThat(payload ->
                payload.contains("\"type\":\"SpotsGenerationRequested\"")
                        && payload.contains("\"showId\":\"show-1\"")
                        && payload.contains("\"sectionId\":\"section-1\"")
                        && payload.contains("\"sectionCode\":\"B\"")
                        && payload.contains("\"totalSpots\":1500")));
    }

    @Test
    void givenUnsupportedEvent_whenPublish_thenIgnores() {
        final DomainEvent event = () -> Instant.now();

        publisher.publish(event);

        verify(kafkaTemplate, never()).sendDefault(any(), any());
    }

    @Test
    void givenSerializationFailure_whenPublish_thenThrows() throws Exception {
        final var event = new SpotsGenerationRequested("show-1", "section-1", "B", 1500, Instant.now());
        final var failingMapper = mock(ObjectMapper.class);
        when(failingMapper.writeValueAsString(any())).thenThrow(new IllegalStateException("mapper down"));
        final var failing = new KafkaDomainEventPublisher(kafkaTemplate, failingMapper);

        assertThrows(IllegalStateException.class, () -> failing.publish(event));
    }
}
