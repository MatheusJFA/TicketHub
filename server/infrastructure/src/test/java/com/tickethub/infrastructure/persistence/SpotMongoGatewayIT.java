package com.tickethub.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.core.spot.SpotID;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Location;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;

@IntegrationTest
class SpotMongoGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SpotMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, SpotDocument.COLLECTION);
    }

    @Test
    void givenASpot_whenCreate_thenPersistsAndFinds() {
        final var spot = Spot.create(Location.create("A1"));

        gateway.create(spot);

        final var found = gateway.findById(spot.getId());
        assertTrue(found.isPresent());
        assertEquals("A1", found.get().getLocation().getValue());
        assertTrue(found.get().isAvailable());
        assertFalse(found.get().isPublished());
    }

    @Test
    void givenASpotWithoutLocation_whenCreate_thenRoundTripsNull() {
        final var spot = Spot.create();

        gateway.create(spot);

        assertNull(gateway.findById(spot.getId()).orElseThrow().getLocation());
    }

    @Test
    void givenASpot_whenUpdate_thenPersistsChanges() {
        final var spot = gateway.create(Spot.create(Location.create("A1")));

        spot.publish();
        spot.changeLocation(Location.create("B2"));
        gateway.update(spot);

        final var found = gateway.findById(spot.getId()).orElseThrow();
        assertTrue(found.isPublished());
        assertEquals("B2", found.getLocation().getValue());
    }

    @Test
    void givenASpot_whenDelete_thenRemoves() {
        final var spot = gateway.create(Spot.create(Location.create("A1")));

        gateway.deleteById(spot.getId());

        assertFalse(gateway.findById(spot.getId()).isPresent());
        assertFalse(gateway.findById(SpotID.generate()).isPresent());
    }

    @Test
    void givenSpots_whenFindAll_thenSearchesByLocation() {
        gateway.create(Spot.create(Location.create("A1")));
        gateway.create(Spot.create(Location.create("A2")));
        gateway.create(Spot.create(Location.create("B1")));

        final var search = gateway.findAll(new SearchQuery(0, 10, "A", "location", "asc"));

        assertEquals(2, search.totalItems());

        final var page = gateway.findAll(new SearchQuery(1, 2, "", "location", "asc"));
        assertEquals(3, page.totalItems());
        assertEquals(1, page.items().size());
    }
}
