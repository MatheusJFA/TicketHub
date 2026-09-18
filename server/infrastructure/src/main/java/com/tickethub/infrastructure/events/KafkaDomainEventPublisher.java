package com.tickethub.infrastructure.events;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.event.DomainEvent;
import com.tickethub.domain.event.DomainEventPublisher;

import tools.jackson.databind.ObjectMapper;

public final class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDomainEventPublisher(final KafkaTemplate<String, String> kafkaTemplate,
            final ObjectMapper objectMapper) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate, "'kafkaTemplate' should not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "'objectMapper' should not be null");
    }

    @Override
    public void publish(final DomainEvent event) {
        Objects.requireNonNull(event, "'event' should not be null");
        if (event instanceof SpotsGenerationRequested requested) {
            final var message = new SpotGenerationMessage(SpotGenerationMessage.TYPE,
                    requested.showId(), requested.sectionId(), requested.sectionCode(),
                    requested.totalSpots());
            try {
                kafkaTemplate.sendDefault(requested.showId(), objectMapper.writeValueAsString(message));
            } catch (final RuntimeException e) {
                throw new IllegalStateException("Failed to publish spot generation event", e);
            }
            return;
        }
        LOG.warn("Ignoring unsupported domain event {}", event.getClass().getSimpleName());
    }
}
