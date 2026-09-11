package com.tickethub.domain.core.section;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Money;

class SectionTest {

    @Test
    void givenValidParams_whenCreate_thenInstantiateSection() {
        final var expectedName = "VIP";
        final var expectedDescription = "A categoria mais assistida";
        final var expectedPublished = true;
        final var expectedTotalSpots = 25L;
        final var expectedTotalSpotsSold = 7L;
        final var expectedPrice = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
        final var expectedSpots = new HashSet<Spot>();

        final var actualSection = Section.create(
                expectedName,
                expectedDescription,
                expectedPublished,
                expectedTotalSpots,
                expectedTotalSpotsSold,
                expectedPrice,
                expectedSpots
        );

        assertNotNull(actualSection);
        assertNotNull(actualSection.getId());
        assertEquals(expectedName, actualSection.getName().getValue());
        assertEquals(expectedDescription, actualSection.getDescription().getValue());
        assertTrue(actualSection.isPublished());
        assertEquals(expectedTotalSpots, actualSection.getTotalSpots());
        assertEquals(expectedTotalSpotsSold, actualSection.getTotalSpotsSold());
        assertEquals(expectedPrice, actualSection.getPrice());
    }

    @Test
    void givenValidParams_whenCreateWithoutPublishFlag_thenInstantiateSectionWithDefaultPublishFalse() {
        final var expectedName = "Comum";
        final var expectedDescription = "Descrição";
        final var expectedTotalSpots = 10L;
        final var expectedPrice = Money.create(new BigDecimal("20.00"), Currency.getInstance("BRL"));
        final var expectedSpots = new HashSet<Spot>();

        final var actualSection = Section.create(expectedName, expectedDescription, expectedTotalSpots, expectedPrice);

        assertNotNull(actualSection);
        assertNotNull(actualSection.getId());
        assertEquals(expectedName, actualSection.getName().getValue());
        assertEquals(expectedDescription, actualSection.getDescription().getValue());
        assertFalse(actualSection.isPublished());
        assertEquals(expectedTotalSpots, actualSection.getTotalSpots());
        assertEquals(0L, actualSection.getTotalSpotsSold());
        assertEquals(expectedPrice, actualSection.getPrice());
    }

    @Test
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedName = "A";
        final var expectedDescription = "Descrição";
        final var expectedPublished = true;
        final var expectedTotalSpots = 10L;
        final var expectedTotalSpotsSold = 0L;
        final var expectedPrice = Money.create(new BigDecimal("20.00"), Currency.getInstance("BRL"));
        final var expectedSpots = new HashSet<Spot>();

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Section.create(
                        expectedName,
                        expectedDescription,
                        expectedPublished,
                        expectedTotalSpots,
                        expectedTotalSpotsSold,
                        expectedPrice,
                        expectedSpots
                )
        );

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }
}
