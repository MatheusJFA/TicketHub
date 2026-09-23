package com.tickethub.infrastructure.events;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.core.section.SpotsGenerationRequested;
import com.tickethub.domain.event.DomainEvent;
import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.infrastructure.exception.EventPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.ObjectMapper;

public final class KafkaDomainEventPublisher implements DomainEventPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaDomainEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDomainEventPublisher(
            final KafkaTemplate<String, String> kafkaTemplate, final ObjectMapper objectMapper) {
        this.kafkaTemplate = requireNonNull(kafkaTemplate, "'kafkaTemplate' should not be null");
        this.objectMapper = requireNonNull(objectMapper, "'objectMapper' should not be null");
    }

    @Override
    public void publish(final DomainEvent event) {
        requireNonNull(event, "'event' should not be null");
        if (event instanceof SpotsGenerationRequested requested) {
            final var message = new SpotGenerationMessage(
                    SpotGenerationMessage.TYPE,
                    requested.showId(),
                    requested.sectionId(),
                    requested.sectionCode(),
                    requested.totalSpots());
            try {
                kafkaTemplate.sendDefault(requested.showId(), objectMapper.writeValueAsString(message));
            } catch (final RuntimeException e) {
                throw new EventPublishException("Failed to publish spot generation event", e);
            }
            return;
        }
        LOG.warn("Ignoring unsupported domain event {}", event.getClass().getSimpleName());
    }
}
