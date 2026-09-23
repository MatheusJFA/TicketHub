package com.tickethub.infrastructure.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.shared.Address;
import com.tickethub.domain.shared.Money;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.show.persistence.ShowDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

@IntegrationTest
@DisplayName("Show Mongo gateway")
class ShowMongoGatewayIT extends ContainerSupport {

    private static final Address ADDRESS = Address.create(
            "Rua do Rock", "s/n", null, "Barra da Tijuca", "Rio de Janeiro", "RJ", "Brasil", "22640-100");
    private static final Money PRICE = Money.create(new BigDecimal("350.00"), Currency.getInstance("BRL"));
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ShowMongoGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(
                mongoTemplate, ShowDocument.COLLECTION, SectionDocument.COLLECTION, SpotDocument.COLLECTION);
    }

    private long count(final String collection) {
        return mongoTemplate.count(new Query(), collection);
    }

    private Show sample() {
        final var show = Show.create("Rock in Rio", "Festival", DATE, ADDRESS, 4, PartnerID.generate());
        show.addSection("Pista Premium", "Perto do palco", 2, PRICE, 5);
        show.addSection("Camarote", "Open bar", 2, PRICE, 5);
        return show;
    }

    @Test
    @DisplayName("Given a show with sections, when create, then bulk persists whole graph")
    void givenAShowWithSections_whenCreate_thenBulkPersistsWholeGraph() {
        final var show = sample();

        gateway.create(show);

        assertEquals(1, count(ShowDocument.COLLECTION));
        assertEquals(2, count(SectionDocument.COLLECTION));
        assertEquals(4, count(SpotDocument.COLLECTION));

        final var found = gateway.findById(show.getId()).orElseThrow();
        assertEquals("Rock in Rio", found.getName().getValue());
        assertEquals("Festival", found.getDescription().getValue());
        assertEquals(DATE, found.getDate());
        assertEquals("Rua do Rock", found.getAddress().getStreet());
        assertEquals(show.getPartnerId(), found.getPartnerId());
        assertEquals(2, found.getSections().size());
        assertTrue(found.getSections().stream()
                .allMatch(section -> section.getSpots().size() == 2));
        assertTrue(found.getSections().stream().allMatch(section -> PRICE.equals(section.getPrice())));
        assertFalse(found.isPublished());
    }

    @Test
    @DisplayName("Given a show, when add section and update, then upserts new children")
    void givenAShow_whenAddSectionAndUpdate_thenUpsertsNewChildren() {
        final var show = gateway.create(sample());

        show.addSection("Backstage", "Acesso total", 3, PRICE, 5);
        show.publishAll();
        gateway.update(show);

        final var found = gateway.findById(show.getId()).orElseThrow();
        assertEquals(3, found.getSections().size());
        assertEquals(
                7,
                found.getSections().stream()
                        .mapToLong(section -> section.getSpots().size())
                        .sum());
        assertTrue(found.isPublished());
        assertTrue(found.getSections().stream().allMatch(section -> section.isPublished()));
        assertEquals(3, count(SectionDocument.COLLECTION));
        assertEquals(7, count(SpotDocument.COLLECTION));
    }

    @Test
    @DisplayName("Given a show, when delete, then cascades whole graph")
    void givenAShow_whenDelete_thenCascadesWholeGraph() {
        final var show = gateway.create(sample());

        gateway.deleteById(show.getId());

        assertFalse(gateway.findById(show.getId()).isPresent());
        assertEquals(0, count(ShowDocument.COLLECTION));
        assertEquals(0, count(SectionDocument.COLLECTION));
        assertEquals(0, count(SpotDocument.COLLECTION));
    }

    @Test
    @DisplayName("Given shell section, when append spots, then bulk inserts and links")
    void givenShellSection_whenAppendSpots_thenBulkInsertsAndLinks() {
        final var show = Show.create("Mega Fest", "Grande", DATE, ADDRESS, 0, PartnerID.generate());
        final var shell = show.addSectionShell("Arena", "Pista", 3, PRICE);
        gateway.create(show);

        final var generated = shell.generateMissingSpots("A", 5);
        gateway.appendSpots(show.getId(), shell.getId(), generated);

        assertEquals(3, count(SpotDocument.COLLECTION));
        final var found = gateway.findById(show.getId()).orElseThrow();
        final var codes = found.getSections().stream()
                .flatMap(section -> section.getSpots().stream())
                .map(spot -> spot.getLocation().getValue())
                .sorted()
                .toList();
        assertEquals(List.of("A00001", "A00002", "A00003"), codes);
    }

    @Test
    @DisplayName("Given shows, when find all, then paginates and searches")
    void givenShows_whenFindAll_thenPaginatesAndSearches() {
        gateway.create(sample());
        gateway.create(Show.create("Jazz Fest", "Suave", DATE, ADDRESS, 0, PartnerID.generate()));

        final var page = gateway.findAll(new SearchQuery(0, 10, "", "name", "asc"));

        assertEquals(2, page.totalItems());
        assertEquals(2, page.items().size());
        assertTrue(page.items().stream().allMatch(show -> show.getPartnerId() != null));

        final var search = gateway.findAll(new SearchQuery(0, 10, "jazz", "name", "asc"));
        assertEquals(1, search.totalItems());
        assertEquals("Jazz Fest", search.items().get(0).getName().getValue());
        assertTrue(search.items().get(0).getSections().isEmpty());
    }

    @Test
    @DisplayName("Given show ids, when exists by ids, then returns only persisted ids")
    void givenShowIds_whenExistsByIds_thenReturnsOnlyPersistedIds() {
        final var first = gateway.create(sample());
        final var second = gateway.create(Show.create("Jazz Fest", "Suave", DATE, ADDRESS, 0, PartnerID.generate()));
        final var missing = ShowID.generate();

        final var existing = gateway.existsByIds(List.of(first.getId(), second.getId(), missing));

        assertEquals(2, existing.size());
        assertTrue(existing.contains(first.getId()));
        assertTrue(existing.contains(second.getId()));
        assertEquals(List.of(), gateway.existsByIds(List.of()));
    }
}
