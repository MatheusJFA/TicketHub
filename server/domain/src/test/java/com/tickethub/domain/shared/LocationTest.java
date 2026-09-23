package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.exception.DomainException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Location")
public class LocationTest {

    @ParameterizedTest(name = "Given null, empty or blank location \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Given null, empty or blank location, when create, then throws DomainException")
    void givenNullEmptyOrBlankLocation_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.create(value),
                () -> "Creating Location with value [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid location", exception.getMessage(), () -> "Exception message should be \"Invalid location\"");
    }

    @ParameterizedTest(name = "Given valid seat label \"{0}\", when create, then stores value")
    @ValueSource(strings = {"A12", "Mesa 5", "Camarote VIP", "B-07", "Balcão 2", "5"})
    @DisplayName("Given valid seat label, when create, then stores value")
    void givenAValidSeatLabel_whenCreate_thenStoreValue(String value) {
        assertEquals(
                value,
                Location.create(value).getValue(),
                () -> "Valid seat label [" + value + "] should be stored as provided");
    }

    @ParameterizedTest(name = "Given label with extra spaces \"{0}\", when create, then normalizes to \"{1}\"")
    @CsvSource(value = {"'  A12  ', A12", "'  Mesa    5  ', Mesa 5", "'  Balcão   2 ', Balcão 2"})
    @DisplayName("Given label with extra spaces, when create, then normalizes value")
    void givenALabelWithExtraSpaces_whenCreate_thenNormalizeValue(String input, String expected) {
        assertEquals(
                expected,
                Location.create(input).getValue(),
                () -> "Label [" + input + "] should normalize to [" + expected + "]");
    }

    @Test
    @DisplayName("Given label with whitespace, when create, then stores single line")
    void givenALabelWithWhitespace_whenCreate_thenStoreSingleLine() {
        assertEquals(
                "Mesa 5",
                Location.create("\tMesa\n  5\r\n").getValue(),
                () -> "Label with whitespace should be stored as single line");
    }

    @ParameterizedTest(name = "Given null or blank location \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n\r", " ", " "})
    @DisplayName("Given null or blank location, when create, then throws DomainException")
    void givenANullOrBlankLocation_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.create(value),
                () -> "Creating Location with blank value [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid location", exception.getMessage(), () -> "Exception message should be \"Invalid location\"");
    }

    @Test
    @DisplayName("Given location, when toString, then returns normalized label")
    void givenALocation_whenToString_thenReturnNormalizedLabel() {
        assertEquals(
                "Mesa 5",
                Location.create("  Mesa   5  ").toString(),
                () -> "Location toString should return normalized label");
    }

    @Test
    @DisplayName("Given same normalized labels, when compare, then are equal with same hashCode")
    void givenSameNormalizedLabels_whenCompare_thenBeEqualAndHaveSameHashCode() {
        Location first = Location.create("Mesa 5");
        Location second = Location.create("  Mesa   5  ");

        assertEquals(first, second, () -> "Same normalized labels should be equal");
        assertEquals(second, first, () -> "Location equality should be symmetric");
        assertEquals(first.hashCode(), second.hashCode(), () -> "Equal Locations should have the same hashCode");
    }

    @ParameterizedTest(name = "Given different label \"{0}\", when compare, then are not equal")
    @ValueSource(strings = {"A13", "B12", "a12"})
    @DisplayName("Given different labels, when compare, then are not equal")
    void givenDifferentLabels_whenCompare_thenNotBeEqual(String value) {
        assertNotEquals(
                Location.create("A12"),
                Location.create(value),
                () -> "Different labels [A12] and [" + value + "] should not be equal");
    }

    @Test
    @DisplayName("Given equivalent locations, when use in set, then find same seat label")
    void givenEquivalentLocations_whenUseInSet_thenFindSameSeatLabel() {
        Set<Location> locations = new HashSet<>();
        locations.add(Location.create("Mesa 5"));
        locations.add(Location.create("  Mesa   5  "));

        assertEquals(1, locations.size(), () -> "Equivalent Locations should collapse to a single set entry");
        assertTrue(locations.contains(Location.create("Mesa 5")), () -> "Set should contain Location [Mesa 5]");
    }

    @Test
    @DisplayName("Given null or different type, when compare, then are not equal")
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        Location location = Location.create("A12");

        assertFalse(location.equals(null), () -> "Location should not be equal to null");
        assertFalse(location.equals("A12"), () -> "Location should not be equal to a String");
        assertFalse(location.equals(Text.create("A12")), () -> "Location should not be equal to a different type");
    }

    @ParameterizedTest(name = "Given section index \"{0}\", when sectionCode, then derives code \"{1}\"")
    @CsvSource(value = {"0, A", "1, B", "25, Z", "26, AA", "27, AB", "51, AZ", "52, BA"})
    @DisplayName("Given section index, when sectionCode, then derives spreadsheet-style code")
    void givenSectionIndex_whenSectionCode_thenDeriveSpreadsheetStyleCode(int index, String expected) {
        assertEquals(
                expected,
                Location.sectionCode(index),
                () -> "Section index [" + index + "] should derive code [" + expected + "]");
    }

    @Test
    @DisplayName("Given negative section index, when sectionCode, then throws DomainException")
    void givenNegativeSectionIndex_whenSectionCode_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.sectionCode(-1),
                () -> "Calling sectionCode with negative index should throw DomainException");

        assertEquals(
                "'sectionIndex' should not be negative",
                exception.getMessage(),
                () -> "Exception message should indicate that sectionIndex must not be negative");
    }

    @ParameterizedTest(
            name = "Given section code \"{0}\" and seat \"{1}\", when generateSeat, then formats code \"{2}\"")
    @CsvSource(value = {"A, 1, A00001", "A, 42, A00042", "B, 7, B00007", "AA, 3, AA00003", "Z, 100000, Z100000"})
    @DisplayName("Given section code and seat, when generateSeat, then formats code")
    void givenSectionCodeAndSeat_whenGenerateSeat_thenFormatCode(String code, long seat, String expected) {
        assertEquals(
                expected,
                Location.generateSeat(code, seat, 5).getValue(),
                () -> "Seat [" + code + ", " + seat + "] should format to [" + expected + "]");
    }

    @ParameterizedTest(name = "Given invalid section code \"{0}\", when generateSeat, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {"a", "A1", "ABCD", "A-1"})
    @DisplayName("Given invalid section code, when generateSeat, then throws DomainException")
    void givenInvalidSectionCode_whenGenerateSeat_thenThrowDomainException(String code) {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.generateSeat(code, 1, 5),
                () -> "Generating seat with invalid section code [" + code + "] should throw DomainException");

        assertEquals(
                "Invalid section code",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid section code\" for [" + code + "]");
    }

    @Test
    @DisplayName("Given zero seat number, when generateSeat, then throws DomainException")
    void givenZeroSeatNumber_whenGenerateSeat_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.generateSeat("A", 0, 5),
                () -> "Generating seat with zero seat number should throw DomainException");

        assertEquals(
                "'seatNumber' should be positive",
                exception.getMessage(),
                () -> "Exception message should indicate that seatNumber must be positive");
    }

    @Test
    @DisplayName("Given no context, when generateRandom, then returns short code")
    void givenNoContext_whenGenerateRandom_thenReturnShortCode() {
        final var first = Location.generateRandom(5);
        final var second = Location.generateRandom(5);

        assertTrue(
                first.getValue().matches("[A-Z]\\d{5}"),
                () -> "Random location should match pattern [A-Z] with 5 digits");
        assertTrue(
                second.getValue().matches("[A-Z]\\d{5}"),
                () -> "Random location should match pattern [A-Z] with 5 digits");
    }

    @ParameterizedTest(name = "Given width \"{0}\", when generateSeat, then pads to width")
    @CsvSource(value = {"A, 7, 3, A007", "B, 42, 6, B000042"})
    @DisplayName("Given custom width, when generateSeat, then pads to width")
    void givenCustomWidth_whenGenerateSeat_thenPadsToWidth(String code, long seat, int width, String expected) {
        assertEquals(
                expected,
                Location.generateSeat(code, seat, width).getValue(),
                () -> "Seat [" + code + ", " + seat + "] with width " + width + " should format to [" + expected + "]");
    }

    @Test
    @DisplayName("Given non-positive width, when generateSeat, then throws DomainException")
    void givenNonPositiveWidth_whenGenerateSeat_thenThrowsDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.generateSeat("A", 1, 0),
                () -> "Generating seat with non-positive width should throw DomainException");

        assertEquals(
                "'seatNumberWidth' should be positive",
                exception.getMessage(),
                () -> "Exception message should indicate that seatNumberWidth must be positive");
    }

    @Test
    @DisplayName("Given non-positive width, when generateRandom, then throws DomainException")
    void givenNonPositiveWidth_whenGenerateRandom_thenThrowsDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Location.generateRandom(0),
                () -> "Generating random location with non-positive width should throw DomainException");

        assertEquals(
                "'seatNumberWidth' should be positive",
                exception.getMessage(),
                () -> "Exception message should indicate that seatNumberWidth must be positive");
    }
}
