package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;

class MongoAuditTrailTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final MongoAuditTrail trail = new MongoAuditTrail(mongoTemplate);

    private static AuditEntry entry() {
        return new AuditEntry(Instant.parse("2027-01-15T20:00:00Z"), "corr-1", "alice",
                "CreateSpotUseCase", "CreateSpotCommand[A1]", AuditOutcome.SUCCESS, null, 12);
    }

    @Test
    void givenAnEntry_whenRecord_thenInsertsDocumentIntoAuditLogs() {
        trail.record(entry());

        final var documents = ArgumentCaptor.forClass(Document.class);
        verify(mongoTemplate).insert(documents.capture(), eq(MongoAuditTrail.COLLECTION));
        final var document = documents.getValue();
        assertEquals(Instant.parse("2027-01-15T20:00:00Z"), document.get("occurredAt"));
        assertEquals("corr-1", document.getString("correlationId"));
        assertEquals("alice", document.getString("actor"));
        assertEquals("CreateSpotUseCase", document.getString("action"));
        assertEquals("CreateSpotCommand[A1]", document.getString("input"));
        assertEquals("SUCCESS", document.getString("outcome"));
        assertEquals(12L, document.getLong("durationMs"));
    }

    @Test
    void givenNullEntry_whenRecord_thenThrows() {
        assertThrows(NullPointerException.class, () -> trail.record(null));
    }

    @Test
    void givenNullTemplate_whenCreate_thenThrows() {
        assertThrows(NullPointerException.class, () -> new MongoAuditTrail(null));
    }
}
