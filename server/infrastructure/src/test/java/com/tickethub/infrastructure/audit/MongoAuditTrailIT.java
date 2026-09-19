package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;

@IntegrationTest
class MongoAuditTrailIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoAuditTrail trail;

    private static AuditEntry entry(final String correlationId) {
        return new AuditEntry(Instant.parse("2027-01-15T20:00:00Z"), correlationId, "alice",
                "CreateSpotUseCase", "CreateSpotCommand[A1]", AuditOutcome.SUCCESS, null, 12);
    }

    @Test
    void givenAnEntry_whenRecord_thenPersistsAllFields() {
        trail.record(entry("corr-1"));

        final var stored = mongoTemplate.findAll(Document.class, MongoAuditTrail.COLLECTION);
        assertEquals(1, stored.size());
        final var document = stored.get(0);
        assertNotNull(document.getObjectId("_id"));
        assertEquals(Instant.parse("2027-01-15T20:00:00Z"), document.getDate("occurredAt").toInstant());
        assertEquals("corr-1", document.getString("correlationId"));
        assertEquals("alice", document.getString("actor"));
        assertEquals("CreateSpotUseCase", document.getString("action"));
        assertEquals("CreateSpotCommand[A1]", document.getString("input"));
        assertEquals("SUCCESS", document.getString("outcome"));
        assertNull(document.get("error"));
    }

    @Test
    void givenEntriesFromPreviousTests_whenStarting_thenCollectionIsClean() {
        assertTrue(mongoTemplate.findAll(Document.class, MongoAuditTrail.COLLECTION).isEmpty());

        trail.record(entry("corr-2"));
        trail.record(entry("corr-3"));

        final var correlationIds = mongoTemplate.findAll(Document.class, MongoAuditTrail.COLLECTION)
                .stream()
                .map(document -> document.getString("correlationId"))
                .toList();
        assertEquals(List.of("corr-2", "corr-3"), correlationIds);
    }
}
