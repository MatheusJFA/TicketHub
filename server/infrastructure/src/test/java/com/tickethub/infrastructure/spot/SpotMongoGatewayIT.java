package com.tickethub.infrastructure.spot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Location;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.spot.SpotMongoGateway;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;

@IntegrationTest
@DisplayName("Spot Mongo gateway")
class SpotMongoGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SpotMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate,
                SectionDocument.COLLECTION, SpotDocument.COLLECTION);
    }

    private SectionID givenSection() {
        final var sectionId = SectionID.generate();
        final var now = Instant.now();
        mongoTemplate.insert(new SectionDocument(sectionId.getValue(), "VIP", "Front stage", false,
                0, 0, null, List.of(), "show-1", "partner-1", now, now, null, null, null),
                SectionDocument.COLLECTION);
        return sectionId;
    }

    @Test
    @DisplayName("Given a spot, when create, then persists and finds with parent links")
    void givenASpot_whenCreate_thenPersistsAndFindsWithParentLinks() {
        final var sectionId = givenSection();
        final var spot = Spot.create(Location.create("A1"));

        gateway.create(spot, sectionId);

        final var found = gateway.findById(spot.getId());
        assertTrue(found.isPresent());
        assertEquals("A1", found.get().getLocation().getValue());
        assertTrue(found.get().isAvailable());
        assertFalse(found.get().isPublished());

        final var stored = mongoTemplate.findById(spot.getId().getValue(),
                SpotDocument.class, SpotDocument.COLLECTION);
        assertEquals("show-1", stored.showId());
        assertEquals(sectionId.getValue(), stored.sectionId());
        assertEquals("partner-1", stored.partnerId());
    }

    @Test
    @DisplayName("Given a spot without location, when create, then generates and round trips code")
    void givenASpotWithoutLocation_whenCreate_thenGeneratesAndRoundTripsCode() {
        final var spot = Spot.create(5);

        gateway.create(spot, givenSection());

        final var found = gateway.findById(spot.getId()).orElseThrow();
        assertNotNull(found.getLocation());
        assertTrue(found.getLocation().getValue().matches("[A-Z]\\d{5}"));
        assertEquals(spot.getLocation(), found.getLocation());
    }

    @Test
    @DisplayName("Given a spot, when update, then persists changes")
    void givenASpot_whenUpdate_thenPersistsChanges() {
        final var spot = gateway.create(Spot.create(Location.create("A1")), givenSection());

        spot.publish();
        spot.changeLocation(Location.create("B2"));
        gateway.update(spot);

        final var found = gateway.findById(spot.getId()).orElseThrow();
        assertTrue(found.isPublished());
        assertEquals("B2", found.getLocation().getValue());
    }

    @Test
    @DisplayName("Given a spot, when delete, then removes")
    void givenASpot_whenDelete_thenRemoves() {
        final var spot = gateway.create(Spot.create(Location.create("A1")), givenSection());

        gateway.deleteById(spot.getId());

        assertFalse(gateway.findById(spot.getId()).isPresent());
        assertFalse(gateway.findById(SpotID.generate()).isPresent());
    }

    @Test
    @DisplayName("Given spots, when find all, then searches by location")
    void givenSpots_whenFindAll_thenSearchesByLocation() {
        final var sectionId = givenSection();
        gateway.create(Spot.create(Location.create("A1")), sectionId);
        gateway.create(Spot.create(Location.create("A2")), sectionId);
        gateway.create(Spot.create(Location.create("B1")), sectionId);

        final var search = gateway.findAll(new SearchQuery(0, 10, "A", "location", "asc"));

        assertEquals(2, search.totalItems());

        final var page = gateway.findAll(new SearchQuery(1, 2, "", "location", "asc"));
        assertEquals(3, page.totalItems());
        assertEquals(1, page.items().size());
    }

    @Test
    @DisplayName("Given spot ids, when exists by ids, then returns only persisted ids")
    void givenSpotIds_whenExistsByIds_thenReturnsOnlyPersistedIds() {
        final var sectionId = givenSection();
        final var first = gateway.create(Spot.create(Location.create("A1")), sectionId);
        final var second = gateway.create(Spot.create(Location.create("A2")), sectionId);
        final var missing = SpotID.generate();

        final var existing = gateway.existsByIds(List.of(first.getId(), second.getId(), missing));

        assertEquals(2, existing.size());
        assertTrue(existing.contains(first.getId()));
        assertTrue(existing.contains(second.getId()));
        assertEquals(List.of(), gateway.existsByIds(List.of()));
    }
}
