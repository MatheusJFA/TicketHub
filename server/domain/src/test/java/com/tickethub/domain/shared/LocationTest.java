package com.tickethub.domain.shared;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class LocationTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void givenNullEmptyOrBlankLocation_whenCreate_thenThrowDomainException(String value) {
        assertEquals("Invalid location", assertThrows(DomainException.class,
                () -> Location.create(value)).getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A12", "Mesa 5", "Camarote VIP", "B-07", "Balcão 2", "5"})
    void givenAValidSeatLabel_whenCreate_thenStoreValue(String value) {
        assertEquals(value, Location.create(value).getValue());
    }

    @ParameterizedTest
    @CsvSource(value = {"'  A12  ', A12", "'  Mesa    5  ', Mesa 5", "'  Balcão   2 ', Balcão 2"})
    void givenALabelWithExtraSpaces_whenCreate_thenNormalizeValue(String input, String expected) {
        assertEquals(expected, Location.create(input).getValue());
    }

    @Test
    void givenALabelWithWhitespace_whenCreate_thenStoreSingleLine() {
        assertEquals("Mesa 5", Location.create("\tMesa\n\u00A0 5\r\n").getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n\r", "\u2003", "\u00A0"})
    void givenANullOrBlankLocation_whenCreate_thenThrowDomainException(String value) {
        DomainException exception = assertThrows(DomainException.class, () -> Location.create(value));

        assertEquals("Invalid location", exception.getMessage());
    }

    @Test
    void givenALocation_whenToString_thenReturnNormalizedLabel() {
        assertEquals("Mesa 5", Location.create("  Mesa   5  ").toString());
    }

    @Test
    void givenSameNormalizedLabels_whenCompare_thenBeEqualAndHaveSameHashCode() {
        Location first = Location.create("Mesa 5");
        Location second = Location.create("  Mesa   5  ");

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {"A13", "B12", "a12"})
    void givenDifferentLabels_whenCompare_thenNotBeEqual(String value) {
        assertNotEquals(Location.create("A12"), Location.create(value));
    }

    @Test
    void givenEquivalentLocations_whenUseInSet_thenFindSameSeatLabel() {
        Set<Location> locations = new HashSet<>();
        locations.add(Location.create("Mesa 5"));
        locations.add(Location.create("  Mesa   5  "));

        assertEquals(1, locations.size());
        assertTrue(locations.contains(Location.create("Mesa 5")));
    }

    @Test
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Location location = Location.create("A12");

        assertFalse(location.equals(null));
        assertFalse(location.equals("A12"));
        assertFalse(location.equals(Text.create("A12")));
    }

    @ParameterizedTest
    @CsvSource(value = {"0, A", "1, B", "25, Z", "26, AA", "27, AB", "51, AZ", "52, BA"})
    void givenSectionIndex_whenSectionCode_thenDeriveSpreadsheetStyleCode(int index, String expected) {
        assertEquals(expected, Location.sectionCode(index));
    }

    @Test
    void givenNegativeSectionIndex_whenSectionCode_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Location.sectionCode(-1));
    }

    @ParameterizedTest
    @CsvSource(value = {"A, 1, A00001", "A, 42, A00042", "B, 7, B00007", "AA, 3, AA00003", "Z, 100000, Z100000"})
    void givenSectionCodeAndSeat_whenGenerateSeat_thenFormatCode(String code, long seat, String expected) {
        assertEquals(expected, Location.generateSeat(code, seat).getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"a", "A1", "ABCD", "A-1"})
    void givenInvalidSectionCode_whenGenerateSeat_thenThrowDomainException(String code) {
        assertThrows(DomainException.class, () -> Location.generateSeat(code, 1));
    }

    @Test
    void givenZeroSeatNumber_whenGenerateSeat_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Location.generateSeat("A", 0));
    }

    @Test
    void givenNoContext_whenGenerateRandom_thenReturnShortCode() {
        final var first = Location.generateRandom();
        final var second = Location.generateRandom();

        assertTrue(first.getValue().matches("[A-Z]\\d{5}"));
        assertTrue(second.getValue().matches("[A-Z]\\d{5}"));
    }
}
