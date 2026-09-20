package com.tickethub.infrastructure.audit;

import static java.util.Objects.requireNonNull;
import java.time.Instant;

public record AuditEntry(
        Instant occurredAt,
        String correlationId,
        String actor,
        String action,
        String input,
        AuditOutcome outcome,
        String error,
        long durationMs) {

    public AuditEntry {
        requireNonNull(occurredAt, "'occurredAt' should not be null");
        requireNonNull(action, "'action' should not be null");
        requireNonNull(outcome, "'outcome' should not be null");
    }
}
