package com.tickethub.infrastructure.audit;

import java.time.Instant;

/**
 * A single audit trail entry as exposed by the read API. Nulls only appear
 * when the stored document predates a field.
 */
public record AuditLogResponse(
        String id,
        Instant occurredAt,
        String correlationId,
        String actor,
        String action,
        String input,
        AuditOutcome outcome,
        String error,
        long durationMs) {}
