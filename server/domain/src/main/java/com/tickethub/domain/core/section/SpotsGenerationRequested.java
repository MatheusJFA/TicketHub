package com.tickethub.domain.core.section;

import java.time.Instant;
import java.util.Objects;

import com.tickethub.domain.event.DomainEvent;

public record SpotsGenerationRequested(
        String showId,
        String sectionId,
        String sectionCode,
        long totalSpots,
        Instant occurredOn) implements DomainEvent {

    public SpotsGenerationRequested {
        Objects.requireNonNull(showId, "'showId' should not be null");
        Objects.requireNonNull(sectionId, "'sectionId' should not be null");
        Objects.requireNonNull(sectionCode, "'sectionCode' should not be null");
        if (totalSpots < 1) {
            throw new IllegalArgumentException("'totalSpots' should be positive");
        }
        Objects.requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
