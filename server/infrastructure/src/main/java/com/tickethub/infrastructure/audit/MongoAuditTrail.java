package com.tickethub.infrastructure.audit;

import static java.util.Objects.requireNonNull;

import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "tickethub.audit.enabled", havingValue = "true", matchIfMissing = true)
public class MongoAuditTrail implements AuditTrail {

    public static final String COLLECTION = "audit_logs";

    private final MongoTemplate mongoTemplate;

    public MongoAuditTrail(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = requireNonNull(mongoTemplate, "'mongoTemplate' should not be null");
    }

    @Override
    public void record(final AuditEntry entry) {
        requireNonNull(entry, "'entry' should not be null");
        final var document = new Document()
                .append("occurredAt", entry.occurredAt())
                .append("correlationId", entry.correlationId())
                .append("actor", entry.actor())
                .append("action", entry.action())
                .append("input", entry.input())
                .append("outcome", entry.outcome().name())
                .append("error", entry.error())
                .append("durationMs", entry.durationMs());
        mongoTemplate.insert(document, COLLECTION);
    }
}
