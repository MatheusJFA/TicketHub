package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

@DisplayName("CPF")
public class CPFTest {

    @ParameterizedTest(name = "Given null, empty or malformed CPF \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "abcdefghijk"})
    @DisplayName("Given null, empty or malformed CPF, when create, then throws DomainException")
    void givenNullEmptyOrMalformedCpf_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(DomainException.class,
                () -> CPF.create(value),
                () -> "Creating CPF with value [" + value + "] should throw DomainException");

        assertEquals("Invalid CPF", exception.getMessage(),
                () -> "Exception message should be \"Invalid CPF\" for value [" + value + "]");
    }

    @Test
    @DisplayName("Given valid CPF, when create, then stores value")
    void givenAValidCPF_whenCreate_thenStoreValue() {
        assertEquals("12345678909", CPF.create("12345678909").getValue(),
                () -> "Valid CPF should be stored as provided");
    }

    @Test
    @DisplayName("Given CPF with all digits equal, when create, then throws DomainException")
    void givenACPFWithAllDigitsEqual_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> CPF.create("11111111111"),
                () -> "Creating CPF with all-equal digits should throw DomainException");

        assertEquals("Invalid CPF", exception.getMessage(),
                () -> "Exception message should be \"Invalid CPF\" for all-equal digits");
    }

    @Test
    @DisplayName("Given CPF with invalid length, when create, then throws DomainException")
    void givenACPFWithInvalidLength_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> CPF.create("1234567890"),
                () -> "Creating CPF with invalid length should throw DomainException");

        assertEquals("Invalid CPF", exception.getMessage(),
                () -> "Exception message should be \"Invalid CPF\" for invalid length");
    }

    @Test
    @DisplayName("Given valid formatted CPF, when create, then stores only digits")
    void givenAValidFormattedCPF_whenCreate_thenStoreOnlyDigits() {
        CPF cpf = CPF.create("123.456.789-09");

        assertEquals("12345678909", cpf.getValue(),
                () -> "Formatted CPF should be normalized to digits only");
    }

    @Test
    @DisplayName("Given valid CPF, when toString, then returns formatted CPF")
    void givenAValidCPF_whenToString_thenReturnFormattedCPF() {
        CPF cpf = CPF.create("12345678909");

        assertEquals("123.456.789-09", cpf.toString(),
                () -> "CPF toString should return formatted value");
    }

    @Test
    @DisplayName("Given invalid CPF, when create, then throws DomainException")
    void givenAnInvalidCPF_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> CPF.create("12345678900"),
                () -> "Creating CPF with invalid check digits should throw DomainException"
        );
        assertEquals(DomainException.class, exception.getClass(),
                () -> "Exception should be of type DomainException");
        assertEquals("Invalid CPF", exception.getMessage(),
                () -> "Exception message should be \"Invalid CPF\"");
    }

    @Test
    @DisplayName("Given null CPF, when create, then throws DomainException")
    void givenANullCPF_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> CPF.create(null),
                () -> "Creating CPF with null should throw DomainException"
        );
        assertEquals(DomainException.class, exception.getClass(),
                () -> "Exception should be of type DomainException");
        assertEquals("Invalid CPF", exception.getMessage(),
                () -> "Exception message should be \"Invalid CPF\" for null value");
    }

    @Test
    @DisplayName("Given formatted and unformatted same CPF, when compare, then are equal")
    void givenFormattedAndUnformattedSameCPF_whenCompare_thenBeEqual() {
        CPF first = CPF.create("12345678909");
        CPF second = CPF.create("123.456.789-09");

        assertEquals(first, second,
                () -> "Formatted and unformatted CPF should be equal");
        assertEquals(first.hashCode(), second.hashCode(),
                () -> "Equal CPFs should have the same hashCode");
    }

    @Test
    @DisplayName("Given different CPFs, when compare, then are not equal")
    void givenDifferentCPFs_whenCompare_thenNotBeEqual() {
        CPF first = CPF.create("12345678909");
        CPF second = CPF.create("52998224725");

        assertNotEquals(first, second,
                () -> "Different CPFs should not be equal");
    }
}
