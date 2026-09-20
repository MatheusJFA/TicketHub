package com.tickethub.domain.core.spot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.exception.SpotAlreadyUsedException;
import com.tickethub.domain.shared.Location;

@DisplayName("Spot")
class SpotTest {

    @Test
    @DisplayName("Given valid location, when create, then instantiate spot")
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
    @DisplayName("Given valid location, when create without flags, then instantiate spot with default values")
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
    @DisplayName("Given invalid location, when create, then throw domain exception")
    void givenInvalidLocation_whenCreate_thenThrowDomainException() {
        final var expectedLocation = "   ";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Location.create(expectedLocation)
        );

        assertEquals("Invalid location", exception.getMessage());
    }

    @Test
    @DisplayName("Given null location, when create, then preserve null location")
    void givenNullLocation_whenCreate_thenPreserveNullLocation() {
        final var actualSpot = Spot.create(null);

        assertNotNull(actualSpot);
        assertNotNull(actualSpot.getId());
        assertNull(actualSpot.getLocation());
    }

    @Test
    @DisplayName("Given no location, when create, then generate short code")
    void givenNoLocation_whenCreate_thenGenerateShortCode() {
        final var actualSpot = Spot.create(5);

        assertNotNull(actualSpot.getLocation());
        assertTrue(actualSpot.getLocation().getValue().matches("[A-Z]\\d{5}"));
    }

    @Test
    @DisplayName("Given available spot, when check in, then marks as used")
    void givenAvailableSpot_whenCheckIn_thenMarksAsUsed() {
        final var spot = Spot.create(Location.create("A1"));

        spot.checkIn();

        assertFalse(spot.isAvailable(),
                () -> "Spot should be unavailable after check-in");
    }

    @Test
    @DisplayName("Given used spot, when check in again, then throws domain exception")
    void givenUsedSpot_whenCheckInAgain_thenThrowsDomainException() {
        final var spot = Spot.create(Location.create("A1"));
        spot.checkIn();

        final var exception = assertThrows(SpotAlreadyUsedException.class, spot::checkIn,
                () -> "Checking in an already used spot should throw");

        assertEquals("Spot is already used", exception.getMessage(),
                () -> "Exception message should indicate the spot was already used");
    }
}
