package com.tickethub.infrastructure.audit;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Filters for reading the audit trail. Every criterion is optional; absent
 * criteria match everything.
 */
public record AuditLogQuery(
        Optional<String> action,
        Optional<String> actor,
        Optional<AuditOutcome> outcome,
        Optional<String> correlationId,
        Optional<Instant> from,
        Optional<Instant> to,
        String searchTerm,
        int page,
        int perPage,
        String sort,
        String direction) {

    public AuditLogQuery {
        Objects.requireNonNull(action, "'action' should not be null");
        Objects.requireNonNull(actor, "'actor' should not be null");
        Objects.requireNonNull(outcome, "'outcome' should not be null");
        Objects.requireNonNull(correlationId, "'correlationId' should not be null");
        Objects.requireNonNull(from, "'from' should not be null");
        Objects.requireNonNull(to, "'to' should not be null");
    }
}
