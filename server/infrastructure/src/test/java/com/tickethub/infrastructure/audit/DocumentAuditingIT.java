package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.shared.Location;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

@IntegrationTest
class DocumentAuditingIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.remove(new Query(), SpotDocument.COLLECTION);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
        mongoTemplate.remove(new Query(), SpotDocument.COLLECTION);
    }

    private SpotDocument stored(final String id) {
        return mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(id)),
                SpotDocument.class, SpotDocument.COLLECTION);
    }

    @Test
    void givenActorInMdc_whenInsert_thenFillsActors() {
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");
        final var spot = Spot.create(Location.create("A1"));

        final var created = mongoTemplate.insert(
                SpotDocument.from(spot, "show-1", "section-1", "partner-1"),
                SpotDocument.COLLECTION);

        final var stored = stored(created.id());
        assertEquals("alice", stored.createdBy());
        assertEquals("alice", stored.lastModifiedBy());
    }

    @Test
    void givenNoActorInMdc_whenInsert_thenFallsBackToSystem() {
        final var spot = Spot.create(Location.create("A1"));

        final var created = mongoTemplate.insert(
                SpotDocument.from(spot, "show-1", "section-1", "partner-1"),
                SpotDocument.COLLECTION);

        final var stored = stored(created.id());
        assertEquals(CorrelationIdFilter.ANONYMOUS_ACTOR, stored.createdBy());
        assertEquals(CorrelationIdFilter.ANONYMOUS_ACTOR, stored.lastModifiedBy());
    }

    @Test
    void givenExistingDocument_whenSave_thenPreservesCreatedByAndUpdatesLastModifiedBy() {
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");
        final var spot = Spot.create(Location.create("A1"));
        final var created = mongoTemplate.insert(
                SpotDocument.from(spot, "show-1", "section-1", "partner-1"),
                SpotDocument.COLLECTION);

        MDC.put(CorrelationIdFilter.ACTOR_KEY, "bob");
        final var loaded = stored(created.id()).toDomain();
        loaded.changeLocation(Location.create("B2"));
        mongoTemplate.save(
                SpotDocument.from(loaded, "show-1", "section-1", "partner-1"),
                SpotDocument.COLLECTION);

        final var stored = stored(created.id());
        assertEquals("alice", stored.createdBy());
        assertEquals("bob", stored.lastModifiedBy());
        assertEquals("B2", stored.location());
    }
}
