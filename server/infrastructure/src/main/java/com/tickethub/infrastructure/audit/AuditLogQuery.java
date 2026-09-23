package com.tickethub.infrastructure.audit;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
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
        requireNonNull(action, "'action' should not be null");
        requireNonNull(actor, "'actor' should not be null");
        requireNonNull(outcome, "'outcome' should not be null");
        requireNonNull(correlationId, "'correlationId' should not be null");
        requireNonNull(from, "'from' should not be null");
        requireNonNull(to, "'to' should not be null");
    }
}
