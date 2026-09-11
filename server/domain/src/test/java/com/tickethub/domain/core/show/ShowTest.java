package com.tickethub.domain.core.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.Entity;
import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Notification;

class ShowTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

    @Test
    void givenValidParams_whenCreate_thenInstantiateShow() {
        final var expectedName = "O Rei Leão";
        final var expectedDescription = "Uma grande apresentação";
        final var expectedPublished = true;
        final var expectedTotalSpots = 50L;
        final var expectedTotalSpotsSold = 12L;
        final var expectedPartnerId = PartnerID.generate();
        final var expectedSections = new HashSet<Section>();

        final var actualShow = Show.create(
                expectedName,
                expectedDescription, DATE,
                expectedPublished,
                expectedTotalSpots,
                expectedTotalSpotsSold,
                expectedPartnerId,
                expectedSections
        );

        assertNotNull(actualShow);
        assertNotNull(actualShow.getId());
        assertEquals(expectedName, actualShow.getName().getValue());
        assertEquals(expectedDescription, actualShow.getDescription().getValue());
        assertTrue(actualShow.isPublished());
        assertEquals(expectedTotalSpots, actualShow.getTotalSpots());
        assertEquals(expectedTotalSpotsSold, actualShow.getTotalSpotsSold());
        assertEquals(expectedPartnerId, actualShow.getPartnerId());
    }

    @Test
    void givenValidParams_whenCreateWithoutPublishFlag_thenInstantiateShowWithDefaultPublishFalse() {
        final var expectedName = "O Rei Leão";
        final var expectedDescription = "Uma grande apresentação";
        final var expectedTotalSpots = 50L;
        final var expectedPartnerId = PartnerID.generate();
        final var expectedSections = new HashSet<Section>();

        final var actualShow = Show.create(expectedName, expectedDescription, DATE, expectedTotalSpots, expectedPartnerId, expectedSections);

        assertNotNull(actualShow);
        assertNotNull(actualShow.getId());
        assertEquals(expectedName, actualShow.getName().getValue());
        assertEquals(expectedDescription, actualShow.getDescription().getValue());
        assertFalse(actualShow.isPublished());
        assertEquals(expectedTotalSpots, actualShow.getTotalSpots());
        assertEquals(0L, actualShow.getTotalSpotsSold());
        assertEquals(expectedPartnerId, actualShow.getPartnerId());
    }

    @Test
    void givenValidShow_whenPublish_thenChangeToPublished() {
        final var expectedPartnerId = PartnerID.generate();
        final var actualShow = Show.create("O Rei Leão", "Uma grande apresentação", DATE, false, 50L, 0L, expectedPartnerId, new HashSet<Section>());

        assertFalse(actualShow.isPublished());

        actualShow.publish();

        assertTrue(actualShow.isPublished());
    }

    @Test
    void givenValidShow_whenUnpublish_thenChangeToUnpublished() {
        final var expectedPartnerId = PartnerID.generate();
        final var actualShow = Show.create("O Rei Leão", "Uma grande apresentação", DATE, true, 50L, 0L, expectedPartnerId, new HashSet<Section>());

        assertTrue(actualShow.isPublished());

        actualShow.unpublish();

        assertFalse(actualShow.isPublished());
    }

    @Test
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedName = "A";
        final var expectedDescription = "Uma grande apresentação";
        final var expectedPublished = true;
        final var expectedTotalSpots = 50L;
        final var expectedTotalSpotsSold = 12L;
        final var expectedPartnerId = PartnerID.generate();
        final var expectedSections = new HashSet<Section>();

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Show.create(
                        expectedName,
                        expectedDescription, DATE,
                        expectedPublished,
                        expectedTotalSpots,
                        expectedTotalSpotsSold,
                        expectedPartnerId,
                        expectedSections
                )
        );

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }



    @Test
    void givenPartner_whenCreateShow_thenAssociatePartnerAndInitializeDefaults() {
        final var partner = Partner.create("Cinema Nova", "11222333000181");
        final var show = partner.createShow("Concert", "Description", DATE, 10);

        assertNotNull(show.getId());
        assertEquals(partner.getId(), show.getPartnerId());
        assertEquals("Concert", show.getName().getValue());
        assertEquals("Description", show.getDescription().getValue());
        assertEquals(10, show.getTotalSpots());
        assertEquals(0, show.getTotalSpotsSold());
        assertFalse(show.isPublished());
        assertTrue(show.getSections().isEmpty());
        final var notification = Notification.create();
        show.validate(notification);
        assertFalse(notification.hasError());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 25})
    void givenCapacity_whenCreateSection_thenGenerateUniqueAvailableUnpublishedSpots(long capacity) {
        final var section = Section.create("VIP", "Description", capacity, PRICE);

        assertEquals(capacity, section.getSpots().size());
        assertEquals(capacity, section.getSpots().stream().map(Spot::getId).distinct().count());
        assertEquals(capacity, section.getTotalSpots());
        assertEquals(0, section.getTotalSpotsSold());
        assertFalse(section.isPublished());
        assertEquals(PRICE, section.getPrice());
        section.getSpots().forEach(spot -> {
            assertNotNull(spot.getId());
            assertTrue(spot.isAvailable());
            assertFalse(spot.isPublished());
            assertNull(spot.getLocation());
        });
    }

    @Test
    void givenNoLocation_whenCreateSpot_thenAllowValidationWithDefaultFlags() {
        final var spot = Spot.create();
        assertNotNull(spot.getId());
        assertNull(spot.getLocation());
        assertTrue(spot.isAvailable());
        assertFalse(spot.isPublished());
        final var notification = Notification.create();
        spot.validate(notification);
        assertFalse(notification.hasError());
    }

    @Test
    void givenShow_whenAddSections_thenAccumulateCapacityAndGenerateSpots() throws Exception {
        final var show = Show.create("Concert", "Description", DATE, 10, PartnerID.generate());
        final var createdAt = show.getCreatedAt();
        resetUpdatedAt(show);

        show.addSection("VIP", "Front seats", 3, PRICE);
        final var firstSection = show.getSections().iterator().next();
        assertEquals("VIP", firstSection.getName().getValue());
        assertEquals("Front seats", firstSection.getDescription().getValue());
        assertEquals(PRICE, firstSection.getPrice());
        assertEquals(3, firstSection.getSpots().size());
        assertFalse(firstSection.isPublished());
        assertTrue(show.getUpdatedAt().isAfter(Instant.EPOCH));
        resetUpdatedAt(show);

        show.addSection("General", "Back seats", 2, PRICE);

        assertEquals(2, show.getSections().size());
        assertTrue(show.getSections().contains(firstSection));
        assertEquals(15, show.getTotalSpots());
        assertEquals(0, show.getTotalSpotsSold());
        assertEquals(5, show.getSections().stream().flatMap(s -> s.getSpots().stream()).map(Spot::getId).distinct().count());
        assertTrue(show.getUpdatedAt().isAfter(Instant.EPOCH));
        assertEquals(createdAt, show.getCreatedAt());
    }

    @Test
    void givenInvalidSectionName_whenAddSection_thenLeaveShowUnchanged() {
        final var show = Show.create("Concert", "Description", DATE, 10, PartnerID.generate());
        final var updatedAt = show.getUpdatedAt();

        final var exception = assertThrows(DomainException.class,
                () -> show.addSection("A", "Description", 2, PRICE));

        assertEquals("Invalid name A", exception.getMessage());
        assertTrue(show.getSections().isEmpty());
        assertEquals(10, show.getTotalSpots());
        assertEquals(updatedAt, show.getUpdatedAt());
    }

    @Test
    void givenShow_whenPublishAndUnpublish_thenUpdateStateAndAudit() throws Exception {
        final var show = Show.create("Concert", "Description", DATE, 0, PartnerID.generate());
        final var createdAt = show.getCreatedAt();
        resetUpdatedAt(show);
        show.publish();
        assertTrue(show.isPublished());
        assertTrue(show.getUpdatedAt().isAfter(Instant.EPOCH));
        resetUpdatedAt(show);
        show.unpublish();
        assertFalse(show.isPublished());
        assertTrue(show.getUpdatedAt().isAfter(Instant.EPOCH));
        assertEquals(createdAt, show.getCreatedAt());
    }

    @Test
    void givenSection_whenPublishAndUnpublish_thenUpdateStateAndAudit() throws Exception {
        final var section = Section.create("VIP", "Description", 1, PRICE);
        final var createdAt = section.getCreatedAt();
        resetUpdatedAt(section);
        section.publish();
        assertTrue(section.isPublished());
        assertTrue(section.getUpdatedAt().isAfter(Instant.EPOCH));
        resetUpdatedAt(section);
        section.unpublish();
        assertFalse(section.isPublished());
        assertTrue(section.getUpdatedAt().isAfter(Instant.EPOCH));
        assertEquals(createdAt, section.getCreatedAt());
    }

    @Test
    void givenSpot_whenPublishAndUnpublish_thenUpdateStateAndAudit() throws Exception {
        final var spot = Spot.create();
        final var createdAt = spot.getCreatedAt();
        resetUpdatedAt(spot);
        spot.publish();
        assertTrue(spot.isPublished());
        assertTrue(spot.getUpdatedAt().isAfter(Instant.EPOCH));
        resetUpdatedAt(spot);
        spot.unpublish();
        assertFalse(spot.isPublished());
        assertTrue(spot.getUpdatedAt().isAfter(Instant.EPOCH));
        assertTrue(spot.isAvailable());
        assertEquals(createdAt, spot.getCreatedAt());
    }

    @Test
    void givenInvalidShow_whenValidate_thenAccumulateErrors() {
        final var show = Show.create("Concert", "Description", DATE, false, -1, -1, null, null);
        final var notification = Notification.create();
        show.validate(notification);
        assertEquals(List.of("'partnerId' should not be null", "'totalSpots' should not be negative",
                "'totalSpotsSold' should not be negative"),
                notification.getErrors().stream().map(error -> error.message()).toList());
    }

    @Test
    void givenInvalidSection_whenValidate_thenAccumulateErrors() {
        final var section = Section.create("VIP", "Description", false, -1, -1, null, null);
        final var notification = Notification.create();
        section.validate(notification);
        assertEquals(List.of("'price' should not be null", "'totalSpots' should not be negative",
                "'totalSpotsSold' should not be negative"),
                notification.getErrors().stream().map(error -> error.message()).toList());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    void givenSectionsWithMixedPublication_whenPublishAll_thenPublishEntireShow(int sectionCount) throws Exception {
        final var show = Show.create("Concert", "Description", DATE, 0, PartnerID.generate());
        for (int i = 0; i < sectionCount; i++) {
            show.addSection("VIP", "Description", i + 1, PRICE);
        }
        final var entities = new ArrayList<Entity<?>>();
        entities.add(show);
        for (final var section : show.getSections()) {
            section.publish();
            section.getSpots().iterator().next().publish();
            entities.add(section);
            entities.addAll(section.getSpots());
        }
        final var createdDates = entities.stream().map(Entity::getCreatedAt).toList();
        for (final var entity : entities) resetUpdatedAt(entity);

        show.publishAll();

        assertTrue(show.isPublished());
        assertEquals(sectionCount, show.getSections().size());
        assertEquals(sectionCount * (sectionCount + 1) / 2, show.getTotalSpots());
        assertEquals(0, show.getTotalSpotsSold());
        for (final var section : show.getSections()) {
            assertTrue(section.isPublished());
            assertEquals(section.getTotalSpots(), section.getSpots().size());
            for (final var spot : section.getSpots()) {
                assertTrue(spot.isPublished());
                assertTrue(spot.isAvailable());
            }
        }
        entities.forEach(entity -> assertTrue(entity.getUpdatedAt().isAfter(Instant.EPOCH)));
        assertEquals(createdDates, entities.stream().map(Entity::getCreatedAt).toList());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 3})
    void givenPublishedShow_whenUnpublishAll_thenUnpublishEntireShow(int sectionCount) throws Exception {
        final var show = Show.create("Concert", "Description", DATE, 0, PartnerID.generate());
        for (int i = 0; i < sectionCount; i++) {
            show.addSection("VIP", "Description", i + 1, PRICE);
        }
        show.publish();
        final var entities = new ArrayList<Entity<?>>();
        entities.add(show);
        for (final var section : show.getSections()) {
            section.publish();
            section.getSpots().forEach(Spot::publish);
            entities.add(section);
            entities.addAll(section.getSpots());
        }
        final var createdDates = entities.stream().map(Entity::getCreatedAt).toList();
        for (final var entity : entities) resetUpdatedAt(entity);

        show.unpublishAll();

        assertFalse(show.isPublished());
        assertEquals(sectionCount, show.getSections().size());
        assertEquals(sectionCount * (sectionCount + 1) / 2, show.getTotalSpots());
        assertEquals(0, show.getTotalSpotsSold());
        for (final var section : show.getSections()) {
            assertFalse(section.isPublished());
            assertEquals(section.getTotalSpots(), section.getSpots().size());
            for (final var spot : section.getSpots()) {
                assertFalse(spot.isPublished());
                assertTrue(spot.isAvailable());
            }
        }
        entities.forEach(entity -> assertTrue(entity.getUpdatedAt().isAfter(Instant.EPOCH)));
        assertEquals(createdDates, entities.stream().map(Entity::getCreatedAt).toList());
    }

    // Seed a known past timestamp so audit assertions do not depend on clock resolution or sleeps.
    private static void resetUpdatedAt(Entity<?> entity) throws Exception {
        final var field = Entity.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(entity, Instant.EPOCH);
    }
}
