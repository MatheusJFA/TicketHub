package com.tickethub.infrastructure.audit;

import java.time.Instant;
import java.util.Objects;

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
        Objects.requireNonNull(occurredAt, "'occurredAt' should not be null");
        Objects.requireNonNull(action, "'action' should not be null");
        Objects.requireNonNull(outcome, "'outcome' should not be null");
    }
}
