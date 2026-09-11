package com.tickethub.domain.core.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.section.Section;
import com.tickethub.domain.exception.DomainException;

class ShowTest {
    private static final OffsetDateTime DATE = OffsetDateTime.parse("2027-01-15T20:00:00-03:00");

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
}
