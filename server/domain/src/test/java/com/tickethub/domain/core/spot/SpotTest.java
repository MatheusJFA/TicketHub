package com.tickethub.domain.core.spot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Location;

class SpotTest {

    @Test
    void givenValidLocation_whenCreate_thenInstantiateSpot() {
        final var expectedLocation = Location.create("A1");
        final var expectedAvailable = true;
        final var expectedPublished = false;

        final var actualSpot = Spot.create(expectedLocation, expectedAvailable, expectedPublished);

        assertNotNull(actualSpot);
        assertNotNull(actualSpot.getId());
        assertEquals(expectedLocation, actualSpot.getLocation());
        assertEquals(expectedAvailable, actualSpot.isAvailable());
        assertEquals(expectedPublished, actualSpot.isPublished());
    }

    @Test
    void givenValidLocation_whenCreateWithoutFlags_thenInstantiateSpotWithDefaultValues() {
        final var expectedLocation = Location.create("A1");

        final var actualSpot = Spot.create(expectedLocation);

        assertNotNull(actualSpot);
        assertNotNull(actualSpot.getId());
        assertEquals(expectedLocation, actualSpot.getLocation());
        assertTrue(actualSpot.isAvailable());
        assertFalse(actualSpot.isPublished());
    }

    @Test
    void givenInvalidLocation_whenCreate_thenThrowDomainException() {
        final var expectedLocation = "   ";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Location.create(expectedLocation)
        );

        assertEquals("Invalid location", exception.getMessage());
    }

    @Test
    void givenNullLocation_whenCreate_thenPreserveNullLocation() {
        final var actualSpot = Spot.create(null);

        assertNotNull(actualSpot);
        assertNotNull(actualSpot.getId());
        assertNull(actualSpot.getLocation());
    }

    @Test
    void givenNoLocation_whenCreate_thenGenerateShortCode() {
        final var actualSpot = Spot.create();

        assertNotNull(actualSpot.getLocation());
        assertTrue(actualSpot.getLocation().getValue().matches("[A-Z]\\d{5}"));
    }
}
