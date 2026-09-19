package com.tickethub.infrastructure.section;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.section.SectionMongoGateway;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
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
                SectionDocument.COLLECTION, SpotDocument.COLLECTION);
    }

    private long count(final String collection) {
        return mongoTemplate.count(new Query(), collection);
    }

    @Test
    void givenASectionWithSpots_whenCreate_thenBulkPersistsEverything() {
        final var section = Section.create("VIP", "Front stage", 3, PRICE);

        gateway.create(section);

        assertEquals(1, count(SectionDocument.COLLECTION));
        assertEquals(3, count(SpotDocument.COLLECTION));

        final var found = gateway.findById(section.getId()).orElseThrow();
        assertEquals("VIP", found.getName().getValue());
        assertEquals(3, found.getSpots().size());
        assertEquals(new BigDecimal("50.00"), found.getPrice().getValue());
        assertEquals("BRL", found.getPrice().getCurrency().getCurrencyCode());
    }

    @Test
    void givenASection_whenUpdate_thenPersistsPriceAndFlags() {
        final var section = gateway.create(Section.create("VIP", "Front stage", 2, PRICE));

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
        final var section = gateway.create(Section.create("VIP", "Front stage", 2, PRICE));

        gateway.deleteById(section.getId());

        assertFalse(gateway.findById(section.getId()).isPresent());
        assertEquals(0, count(SectionDocument.COLLECTION));
        assertEquals(0, count(SpotDocument.COLLECTION));
    }

    @Test
    void givenSections_whenFindAll_thenReturnsSectionsWithSpots() {
        gateway.create(Section.create("VIP", "Front stage", 2, PRICE));
        gateway.create(Section.create("Pista", "Geral", 1, PRICE));

        final var page = gateway.findAll(new SearchQuery(0, 10, "", "name", "asc"));

        assertEquals(2, page.totalItems());
        assertEquals(2, page.items().size());
        assertTrue(page.items().stream().allMatch(section -> !section.getSpots().isEmpty()));
        assertEquals(1, gateway.findAll(new SearchQuery(0, 10, "pista", "name", "asc")).totalItems());
    }
}
