package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;

@IntegrationTest
class MongoAuditLogReaderIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoAuditLogReader reader;

    @BeforeEach
    void setUp() {
        mongoTemplate.remove(new Query(), MongoAuditTrail.COLLECTION);
        seed("corr-1", "alice", "DefaultCreateSpotUseCase", "SUCCESS", "2027-01-10T10:00:00Z");
        seed("corr-2", "bob", "DefaultLoginUseCase", "UNAUTHORIZED", "2027-01-11T10:00:00Z");
        seed("corr-3", "alice", "DefaultCreateSpotUseCase", "VALIDATION_ERROR", "2027-01-12T10:00:00Z");
    }

    @AfterEach
    void tearDown() {
        mongoTemplate.remove(new Query(), MongoAuditTrail.COLLECTION);
    }

    private void seed(final String correlationId, final String actor, final String action,
            final String outcome, final String occurredAt) {
        mongoTemplate.insert(new Document()
                .append("occurredAt", Date.from(Instant.parse(occurredAt)))
                .append("correlationId", correlationId)
                .append("actor", actor)
                .append("action", action)
                .append("input", "input")
                .append("outcome", outcome)
                .append("error", null)
                .append("durationMs", 5L), MongoAuditTrail.COLLECTION);
    }

    private static AuditLogQuery unfiltered() {
        return new AuditLogQuery(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), "", 0, 10, "occurredAt", "asc");
    }

    @Test
    void givenNoFilters_whenSearch_thenReturnsAllEntries() {
        final var page = reader.search(unfiltered());

        assertEquals(3, page.totalItems());
        assertEquals(3, page.items().size());
    }

    @Test
    void givenActionAndActor_whenSearch_thenFilters() {
        final var query = new AuditLogQuery(Optional.of("DefaultCreateSpotUseCase"),
                Optional.of("alice"), Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), "", 0, 10, "occurredAt", "asc");

        final var page = reader.search(query);

        assertEquals(2, page.totalItems());
        assertTrue(page.items().stream().map(AuditLogResponse::correlationId)
                .toList().containsAll(List.of("corr-1", "corr-3")));
    }

    @Test
    void givenOutcome_whenSearch_thenFilters() {
        final var query = new AuditLogQuery(Optional.empty(), Optional.empty(),
                Optional.of(AuditOutcome.UNAUTHORIZED), Optional.empty(), Optional.empty(),
                Optional.empty(), "", 0, 10, "occurredAt", "asc");

        final var page = reader.search(query);

        assertEquals(1, page.totalItems());
        assertEquals("corr-2", page.items().get(0).correlationId());
    }

    @Test
    void givenDateRange_whenSearch_thenFilters() {
        final var query = new AuditLogQuery(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.of(Instant.parse("2027-01-11T00:00:00Z")),
                Optional.of(Instant.parse("2027-01-11T23:59:59Z")), "", 0, 10, "occurredAt", "asc");

        final var page = reader.search(query);

        assertEquals(1, page.totalItems());
        assertEquals("corr-2", page.items().get(0).correlationId());
    }

    @Test
    void givenPagination_whenSearch_thenReturnsRequestedPage() {
        final var query = new AuditLogQuery(Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(), "", 1, 2, "occurredAt", "asc");

        final var page = reader.search(query);

        assertEquals(3, page.totalItems());
        assertEquals(1, page.items().size());
        assertEquals("corr-3", page.items().get(0).correlationId());
    }
}
