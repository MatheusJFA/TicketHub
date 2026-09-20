package com.tickethub.domain.core.section;

import static java.util.Objects.requireNonNull;
import java.time.Instant;

import com.tickethub.domain.event.DomainEvent;

public record SpotsGenerationRequested(
        String showId,
        String sectionId,
        String sectionCode,
        long totalSpots,
        Instant occurredOn) implements DomainEvent {

    public SpotsGenerationRequested {
        requireNonNull(showId, "'showId' should not be null");
        requireNonNull(sectionId, "'sectionId' should not be null");
        requireNonNull(sectionCode, "'sectionCode' should not be null");
        if (totalSpots < 1) {
            throw new IllegalArgumentException("'totalSpots' should be positive");
        }
        requireNonNull(occurredOn, "'occurredOn' should not be null");
    }
}
