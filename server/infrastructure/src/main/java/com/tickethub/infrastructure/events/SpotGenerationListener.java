package com.tickethub.infrastructure.events;

import java.util.Objects;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.tickethub.application.section.generatespots.GenerateSectionSpotsCommand;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsUseCase;

import tools.jackson.databind.ObjectMapper;

@Component
public class SpotGenerationListener {

    private final GenerateSectionSpotsUseCase generateSectionSpots;
    private final ObjectMapper objectMapper;

    public SpotGenerationListener(final GenerateSectionSpotsUseCase generateSectionSpots,
            final ObjectMapper objectMapper) {
        this.generateSectionSpots = Objects.requireNonNull(generateSectionSpots,
                "'generateSectionSpots' should not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "'objectMapper' should not be null");
    }

    @KafkaListener(topics = "${tickethub.kafka.topic.name}")
    public void onMessage(final String payload) {
        final SpotGenerationMessage message;
        try {
            message = objectMapper.readValue(payload, SpotGenerationMessage.class);
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Invalid spot generation message", e);
        }
        if (!SpotGenerationMessage.TYPE.equals(message.type())) {
            return;
        }
        final var result = generateSectionSpots.execute(new GenerateSectionSpotsCommand(
                message.showId(), message.sectionId(), message.sectionCode()));
        if (result.isLeft()) {
            final var notification = result.getLeft();
            final var first = notification.firstError();
            throw new IllegalStateException(
                    "Spot generation failed: " + (first == null ? "unknown" : first.message()),
                    notification.getCause());
        }
    }
}
