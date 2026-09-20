package com.tickethub.infrastructure.events;

import static java.util.Objects.requireNonNull;
import java.util.Optional;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.tickethub.application.section.generatespots.GenerateSectionSpotsCommand;
import com.tickethub.application.section.generatespots.GenerateSectionSpotsUseCase;
import com.tickethub.infrastructure.exception.SpotGenerationException;

import tools.jackson.databind.ObjectMapper;

@Component
public class SpotGenerationListener {

    private final GenerateSectionSpotsUseCase generateSectionSpots;
    private final ObjectMapper objectMapper;

    public SpotGenerationListener(final GenerateSectionSpotsUseCase generateSectionSpots,
            final ObjectMapper objectMapper) {
        this.generateSectionSpots = requireNonNull(generateSectionSpots,
                "'generateSectionSpots' should not be null");
        this.objectMapper = requireNonNull(objectMapper, "'objectMapper' should not be null");
    }

    @KafkaListener(topics = "${tickethub.kafka.topic.name}")
    public void onMessage(final String payload) {
        final SpotGenerationMessage message;
        try {
            message = objectMapper.readValue(payload, SpotGenerationMessage.class);
        } catch (final RuntimeException e) {
            throw new SpotGenerationException("Invalid spot generation message", e);
        }
        if (!SpotGenerationMessage.TYPE.equals(message.type())) {
            return;
        }
        final var result = generateSectionSpots.execute(new GenerateSectionSpotsCommand(
                message.showId(), message.sectionId(), message.sectionCode()));
        if (result.isLeft()) {
            final var notification = result.getLeft();
            final var detail = Optional.ofNullable(notification.firstError())
                    .map(first -> first.message()).orElse("unknown");
            throw new SpotGenerationException("Spot generation failed: " + detail,
                    notification.getCause());
        }
    }
}
