package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CNPJ")
public class CNPJTest {

    @ParameterizedTest(name = "Given null, empty or malformed CNPJ \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "abcdefghijklmn"})
    @DisplayName("Given null, empty or malformed CNPJ, when create, then throws DomainException")
    void givenNullEmptyOrMalformedCnpj_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> CNPJ.create(value),
                () -> "Creating CNPJ with value [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid CNPJ",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid CNPJ\" for value [" + value + "]");
    }

    @ParameterizedTest(name = "Given valid CNPJ \"{0}\", when create, then stores value")
    @ValueSource(strings = {"11222333000181", "04252011000110", "11444777000161", "12ABC34501DE35"})
    @DisplayName("Given valid CNPJ, when create, then stores value")
    void givenAValidCNPJ_whenCreate_thenStoreValue(String value) {
        assertEquals(value, CNPJ.create(value).getValue(), () -> "CNPJ value should be stored as [" + value + "]");
    }

    @Test
    @DisplayName("Given valid formatted CNPJ, when create, then stores only digits")
    void givenAValidFormattedCNPJ_whenCreate_thenStoreOnlyDigits() {
        CNPJ cnpj = CNPJ.create("11.222.333/0001-81");

        assertEquals("11222333000181", cnpj.getValue(), () -> "Formatted CNPJ should be normalized to digits only");
    }

    @Test
    @DisplayName("Given formatted alphanumeric CNPJ, when create, then preserves letters")
    void givenAFormattedAlphanumericCNPJ_whenCreate_thenPreserveLetters() {
        CNPJ cnpj = CNPJ.create("12.ABC.345/01DE-35");

        assertEquals(
                "12ABC34501DE35",
                cnpj.getValue(),
                () -> "Alphanumeric CNPJ should preserve letters after normalization");
    }

    @Test
    @DisplayName("Given formatted CNPJ with spaces, when create, then normalizes value")
    void givenAFormattedCNPJWithSpaces_whenCreate_thenNormalizeValue() {
        assertEquals(
                "04252011000110",
                CNPJ.create("  04.252.011/0001-10  ").getValue(),
                () -> "CNPJ with surrounding spaces should be trimmed and normalized");
    }

    @ParameterizedTest(name = "Given CNPJ with invalid length \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "./-", "1122233300018", "112223330001810"})
    @DisplayName("Given CNPJ with invalid length, when create, then throws DomainException")
    void givenACNPJWithInvalidLength_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> CNPJ.create(value),
                () -> "Creating CNPJ with invalid length [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid CNPJ",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid CNPJ\" for value [" + value + "]");
    }

    @ParameterizedTest(name = "Given CNPJ with all digits equal \"{0}\", when create, then throws DomainException")
    @ValueSource(
            strings = {
                "00000000000000", "11111111111111", "22222222222222", "33333333333333",
                "44444444444444", "55555555555555", "66666666666666", "77777777777777",
                "88888888888888", "99999999999999"
            })
    @DisplayName("Given CNPJ with all digits equal, when create, then throws DomainException")
    void givenACNPJWithAllDigitsEqual_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> CNPJ.create(value),
                () -> "Creating CNPJ with all-equal digits [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid CNPJ",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid CNPJ\" for value [" + value + "]");
    }

    @ParameterizedTest(name = "Given CNPJ with invalid check digits \"{0}\", when create, then throws DomainException")
    @ValueSource(strings = {"11222333000191", "11222333000182", "12ABC34501DE45", "12ABC34501DE36"})
    @DisplayName("Given CNPJ with invalid check digits, when create, then throws DomainException")
    void givenACNPJWithInvalidCheckDigits_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> CNPJ.create(value),
                () -> "Creating CNPJ with invalid check digits [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid CNPJ",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid CNPJ\" for value [" + value + "]");
    }

    @ParameterizedTest(name = "Given CNPJ with invalid characters \"{0}\", when create, then throws DomainException")
    @ValueSource(strings = {"1122233300018A", "12ABC34501DE3A", "@11222333000181", "11222333000181!", "12@BC34501DE35"})
    @DisplayName("Given CNPJ with invalid characters, when create, then throws DomainException")
    void givenACNPJWithInvalidCharacters_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(
                DomainException.class,
                () -> CNPJ.create(value),
                () -> "Creating CNPJ with invalid characters [" + value + "] should throw DomainException");

        assertEquals(
                "Invalid CNPJ",
                exception.getMessage(),
                () -> "Exception message should be \"Invalid CNPJ\" for value [" + value + "]");
    }

    @Test
    @DisplayName("Given valid CNPJ, when toString, then returns formatted CNPJ")
    void givenAValidCNPJ_whenToString_thenReturnFormattedCNPJ() {
        assertEquals(
                "11.222.333/0001-81",
                CNPJ.create("11222333000181").toString(),
                () -> "CNPJ toString should return formatted value");
    }

    @Test
    @DisplayName("Given alphanumeric CNPJ, when toString, then returns formatted CNPJ")
    void givenAnAlphanumericCNPJ_whenToString_thenReturnFormattedCNPJ() {
        assertEquals(
                "12.ABC.345/01DE-35",
                CNPJ.create("12ABC34501DE35").toString(),
                () -> "Alphanumeric CNPJ toString should return formatted value");
    }

    @ParameterizedTest(name = "Given formatted and unformatted same CNPJ \"{0}\", when compare, then are equal")
    @ValueSource(strings = {"11.222.333/0001-81", "12.ABC.345/01DE-35"})
    @DisplayName("Given formatted and unformatted same CNPJ, when compare, then are equal")
    void givenFormattedAndUnformattedSameCNPJ_whenCompare_thenBeEqual(String value) {
        CNPJ first = CNPJ.create(value);
        CNPJ second = CNPJ.create(first.getValue());

        assertEquals(first, second, () -> "Formatted and unformatted CNPJ [" + value + "] should be equal");
        assertEquals(second, first, () -> "CNPJ equality should be symmetric for [" + value + "]");
        assertEquals(
                first.hashCode(),
                second.hashCode(),
                () -> "Equal CNPJs should have the same hashCode for [" + value + "]");
    }

    @Test
    @DisplayName("Given different CNPJs, when compare, then are not equal")
    void givenDifferentCNPJs_whenCompare_thenNotBeEqual() {
        assertNotEquals(
                CNPJ.create("11222333000181"),
                CNPJ.create("04252011000110"),
                () -> "Different CNPJs should not be equal");
    }

    @Test
    @DisplayName("Given null or different type, when compare, then are not equal")
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        CNPJ cnpj = CNPJ.create("11222333000181");

        assertFalse(cnpj.equals(null), () -> "CNPJ should not be equal to null");
        assertFalse(cnpj.equals(cnpj.getValue()), () -> "CNPJ should not be equal to a different type");
    }
}
