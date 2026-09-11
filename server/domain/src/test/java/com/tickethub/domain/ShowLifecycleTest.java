package com.tickethub.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.core.partner.Partner;
import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Money;
import com.tickethub.domain.validation.Notification;

class ShowLifecycleTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");
    private static final Money PRICE = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

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

    // Seed a known past timestamp so audit assertions do not depend on clock resolution or sleeps.
    private static void resetUpdatedAt(Entity<?> entity) throws Exception {
        final var field = Entity.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(entity, Instant.EPOCH);
    }
}
