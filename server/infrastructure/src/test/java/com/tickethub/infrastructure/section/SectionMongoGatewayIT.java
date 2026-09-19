package com.tickethub.infrastructure.section;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.section.SectionID;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.section.SectionMongoGateway;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;

@IntegrationTest
class SectionMongoGatewayIT extends ContainerSupport {

    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private SectionMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate,
                ShowDocument.COLLECTION, SectionDocument.COLLECTION, SpotDocument.COLLECTION);
    }

    private long count(final String collection) {
        return mongoTemplate.count(new Query(), collection);
    }

    private ShowID givenShow() {
        final var showId = ShowID.generate();
        final var now = Instant.now();
        mongoTemplate.insert(new ShowDocument(showId.getValue(), "Show", "Description", null, null,
                false, 0, 0, "partner-1", List.of(), now, now, null, null, null),
                ShowDocument.COLLECTION);
        return showId;
    }

    @Test
    void givenASectionWithSpots_whenCreate_thenBulkPersistsEverythingWithParentLinks() {
        final var showId = givenShow();
        final var section = Section.create("VIP", "Front stage", 3, PRICE);

        gateway.create(section, showId);

        assertEquals(1, count(SectionDocument.COLLECTION));
        assertEquals(3, count(SpotDocument.COLLECTION));

        final var storedSection = mongoTemplate.findById(section.getId().getValue(),
                SectionDocument.class, SectionDocument.COLLECTION);
        assertEquals(showId.getValue(), storedSection.showId());
        assertEquals("partner-1", storedSection.partnerId());

        final var storedSpots = mongoTemplate.find(new Query(), SpotDocument.class,
                SpotDocument.COLLECTION);
        assertTrue(storedSpots.stream().allMatch(spot ->
                showId.getValue().equals(spot.showId())
                        && section.getId().getValue().equals(spot.sectionId())
                        && "partner-1".equals(spot.partnerId())));

        final var found = gateway.findById(section.getId()).orElseThrow();
        assertEquals("VIP", found.getName().getValue());
        assertEquals(3, found.getSpots().size());
        assertEquals(new BigDecimal("50.00"), found.getPrice().getValue());
        assertEquals("BRL", found.getPrice().getCurrency().getCurrencyCode());
    }

    @Test
    void givenASection_whenUpdate_thenPersistsPriceAndFlags() {
        final var section = gateway.create(Section.create("VIP", "Front stage", 2, PRICE), givenShow());

        section.changePrice(Money.create(new BigDecimal("99.90"), Currency.getInstance("BRL")));
        section.publishAll();
        gateway.update(section);

        final var found = gateway.findById(section.getId()).orElseThrow();
        assertTrue(found.isPublished());
        assertEquals(new BigDecimal("99.90"), found.getPrice().getValue());
        assertTrue(found.getSpots().stream().allMatch(spot -> spot.isPublished()));
    }

    @Test
    void givenASection_whenDelete_thenCascadesSpots() {
        final var section = gateway.create(Section.create("VIP", "Front stage", 2, PRICE), givenShow());

        gateway.deleteById(section.getId());

        assertFalse(gateway.findById(section.getId()).isPresent());
        assertEquals(0, count(SectionDocument.COLLECTION));
        assertEquals(0, count(SpotDocument.COLLECTION));
    }

    @Test
    void givenSections_whenFindAll_thenReturnsSectionsWithSpots() {
        final var showId = givenShow();
        gateway.create(Section.create("VIP", "Front stage", 2, PRICE), showId);
        gateway.create(Section.create("Pista", "Geral", 1, PRICE), showId);

        final var page = gateway.findAll(new SearchQuery(0, 10, "", "name", "asc"));

        assertEquals(2, page.totalItems());
        assertEquals(2, page.items().size());
        assertTrue(page.items().stream().allMatch(section -> !section.getSpots().isEmpty()));
        assertEquals(1, gateway.findAll(new SearchQuery(0, 10, "pista", "name", "asc")).totalItems());
    }

    @Test
    void givenSectionIds_whenExistsByIds_thenReturnsOnlyPersistedIds() {
        final var showId = givenShow();
        final var first = gateway.create(Section.create("VIP", "Front stage", 1, PRICE), showId);
        final var second = gateway.create(Section.create("Pista", "Geral", 1, PRICE), showId);
        final var missing = SectionID.generate();

        final var existing = gateway.existsByIds(List.of(first.getId(), second.getId(), missing));

        assertEquals(2, existing.size());
        assertTrue(existing.contains(first.getId()));
        assertTrue(existing.contains(second.getId()));
        assertEquals(List.of(), gateway.existsByIds(List.of()));
    }
}
