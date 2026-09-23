package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;

@DisplayName("MongoAuditTrail")
class MongoAuditTrailTest {

    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final MongoAuditTrail trail = new MongoAuditTrail(mongoTemplate);

    private static AuditEntry entry() {
        return new AuditEntry(
                Instant.parse("2027-01-15T20:00:00Z"),
                "corr-1",
                "alice",
                "CreateSpotUseCase",
                "CreateSpotCommand[A1]",
                AuditOutcome.SUCCESS,
                null,
                12);
    }

    @Test
    @DisplayName("Given an entry, when record, then inserts document into audit logs")
    void givenAnEntry_whenRecord_thenInsertsDocumentIntoAuditLogs() {
        trail.record(entry());

        final var documents = ArgumentCaptor.forClass(Document.class);
        verify(mongoTemplate).insert(documents.capture(), eq(MongoAuditTrail.COLLECTION));
        final var document = documents.getValue();
        assertEquals(
                Instant.parse("2027-01-15T20:00:00Z"),
                document.get("occurredAt"),
                () -> "Recorded document should preserve the entry occurredAt");
        assertEquals(
                "corr-1",
                document.getString("correlationId"),
                () -> "Recorded document should preserve the entry correlationId");
        assertEquals("alice", document.getString("actor"), () -> "Recorded document should preserve the entry actor");
        assertEquals(
                "CreateSpotUseCase",
                document.getString("action"),
                () -> "Recorded document should preserve the entry action");
        assertEquals(
                "CreateSpotCommand[A1]",
                document.getString("input"),
                () -> "Recorded document should preserve the entry input");
        assertEquals(
                "SUCCESS", document.getString("outcome"), () -> "Recorded document should preserve the entry outcome");
        assertEquals(
                12L, document.getLong("durationMs"), () -> "Recorded document should preserve the entry durationMs");
    }

    @Test
    @DisplayName("Given null entry, when record, then throws NullPointerException")
    void givenNullEntry_whenRecord_thenThrows() {
        final var exception = assertThrows(
                NullPointerException.class,
                () -> trail.record(null),
                () -> "Recording a null entry should throw NullPointerException");

        assertEquals(
                "'entry' should not be null",
                exception.getMessage(),
                () -> "Exception message should indicate that entry must not be null");
    }

    @Test
    @DisplayName("Given null template, when create, then throws NullPointerException")
    void givenNullTemplate_whenCreate_thenThrows() {
        final var exception = assertThrows(
                NullPointerException.class,
                () -> new MongoAuditTrail(null),
                () -> "Creating MongoAuditTrail with null mongoTemplate should throw NullPointerException");

        assertEquals(
                "'mongoTemplate' should not be null",
                exception.getMessage(),
                () -> "Exception message should indicate that mongoTemplate must not be null");
    }
}
