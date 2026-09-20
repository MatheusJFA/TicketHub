package com.tickethub.domain.core.section;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.Entity;
import com.tickethub.domain.core.spot.Spot;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.shared.Money;

@DisplayName("Section")
class SectionTest {

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 3})
    @DisplayName("Given spots with mixed publication, when publish all, then publish section and every spot")
    void givenSpotsWithMixedPublication_whenPublishAll_thenPublishSectionAndEverySpot(long capacity) throws Exception {
        final var price = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
        final var section = Section.create("VIP", "Description", capacity, price, "A", 5);
        if (capacity > 0) section.getSpots().iterator().next().publish();
        final var entities = new ArrayList<Entity<?>>();
        entities.add(section);
        entities.addAll(section.getSpots());
        final var createdDates = entities.stream().map(Entity::getCreatedAt).toList();
        for (final var entity : entities) resetUpdatedAt(entity);

        section.publishAll();

        assertTrue(section.isPublished());
        assertEquals(capacity, section.getSpots().size());
        assertEquals(capacity, section.getTotalSpots());
        assertEquals(0, section.getTotalSpotsSold());
        assertEquals(price, section.getPrice());
        for (final var spot : section.getSpots()) {
            assertTrue(spot.isPublished());
            assertTrue(spot.isAvailable());
        }
        entities.forEach(entity -> assertTrue(entity.getUpdatedAt().isAfter(Instant.EPOCH)));
        assertEquals(createdDates, entities.stream().map(Entity::getCreatedAt).toList());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1, 3})
    @DisplayName("Given published section, when unpublish all, then unpublish section and every spot")
    void givenPublishedSection_whenUnpublishAll_thenUnpublishSectionAndEverySpot(long capacity) throws Exception {
        final var price = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
        final var section = Section.create("VIP", "Description", capacity, price, "A", 5);
        section.publish();
        section.getSpots().forEach(Spot::publish);
        final var entities = new ArrayList<Entity<?>>();
        entities.add(section);
        entities.addAll(section.getSpots());
        final var createdDates = entities.stream().map(Entity::getCreatedAt).toList();
        for (final var entity : entities) resetUpdatedAt(entity);

        section.unpublishAll();

        assertFalse(section.isPublished());
        assertEquals(capacity, section.getSpots().size());
        assertEquals(capacity, section.getTotalSpots());
        assertEquals(0, section.getTotalSpotsSold());
        assertEquals(price, section.getPrice());
        for (final var spot : section.getSpots()) {
            assertFalse(spot.isPublished());
            assertTrue(spot.isAvailable());
        }
        entities.forEach(entity -> assertTrue(entity.getUpdatedAt().isAfter(Instant.EPOCH)));
        assertEquals(createdDates, entities.stream().map(Entity::getCreatedAt).toList());
    }

    private static void resetUpdatedAt(Entity<?> entity) throws Exception {
        final var field = Entity.class.getDeclaredField("updatedAt");
        field.setAccessible(true);
        field.set(entity, Instant.EPOCH);
    }

    @Test
    @DisplayName("Given valid params, when create, then instantiate section")
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
    @DisplayName("Given valid params, when create without publish flag, then instantiate section with default publish false")
    void givenValidParams_whenCreateWithoutPublishFlag_thenInstantiateSectionWithDefaultPublishFalse() {
        final var expectedName = "Comum";
        final var expectedDescription = "Descrição";
        final var expectedTotalSpots = 10L;
        final var expectedPrice = Money.create(new BigDecimal("20.00"), Currency.getInstance("BRL"));
        final var expectedSpots = new HashSet<Spot>();

        final var actualSection = Section.create(expectedName, expectedDescription, expectedTotalSpots, expectedPrice, "A", 5);

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
    @DisplayName("Given invalid name, when create, then throw domain exception")
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

    @Test
    @DisplayName("Given capacity, when create, then generate sequential seat codes")
    void givenCapacity_whenCreate_thenGenerateSequentialSeatCodes() {
        final var price = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

        final var section = Section.create("VIP", "Front stage", 3, price, "B", 5);

        final var codes = section.getSpots().stream()
                .map(spot -> spot.getLocation().getValue())
                .sorted()
                .toList();
        assertEquals(List.of("B00001", "B00002", "B00003"), codes);
    }

    @Test
    @DisplayName("Given capacity, when create without code, then default to first section")
    void givenCapacity_whenCreateWithoutCode_thenDefaultToFirstSection() {
        final var price = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));

        final var section = Section.create("VIP", "Front stage", 2, price, "A", 5);

        final var codes = section.getSpots().stream()
                .map(spot -> spot.getLocation().getValue())
                .sorted()
                .toList();
        assertEquals(List.of("A00001", "A00002"), codes);
    }

    @Test
    @DisplayName("Given shell, when generate missing spots, then complete sequentially")
    void givenShell_whenGenerateMissingSpots_thenCompleteSequentially() {
        final var price = Money.create(new BigDecimal("50.00"), Currency.getInstance("BRL"));
        final var section = Section.createShell("VIP", "Front stage", 3, price);

        assertTrue(section.getSpots().isEmpty());

        section.generateMissingSpots("C", 5);

        final var codes = section.getSpots().stream()
                .map(spot -> spot.getLocation().getValue())
                .sorted()
                .toList();
        assertEquals(List.of("C00001", "C00002", "C00003"), codes);

        section.generateMissingSpots("C", 5);
        assertEquals(3, section.getSpots().size());
    }
}
