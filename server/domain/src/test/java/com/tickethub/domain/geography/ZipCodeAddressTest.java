package com.tickethub.domain.geography;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("ZipCodeAddress")
class ZipCodeAddressTest {

    @Test
    @DisplayName("Given formatted zip, when normalize, then returns only digits")
    void givenFormattedZip_whenNormalize_thenReturnsDigits() {
        assertEquals(
                "01305000",
                ZipCodeAddress.normalize("01305-000"),
                () -> "Normalized ZIP code should strip formatting and keep only digits");
    }

    @Test
    @DisplayName("Given digits-only zip, when normalize, then returns same digits")
    void givenDigitsOnlyZip_whenNormalize_thenReturnsDigits() {
        assertEquals(
                "01305000",
                ZipCodeAddress.normalize("01305000"),
                () -> "Normalized ZIP code should preserve digits-only input");
    }

    @ParameterizedTest(name = "Given invalid zip \"{0}\", when normalize, then returns null")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "123456789", "abcdefgh"})
    @DisplayName("Given invalid zip, when normalize, then returns null")
    void givenInvalidZip_whenNormalize_thenReturnsNull(String zipCode) {
        assertEquals(
                null,
                ZipCodeAddress.normalize(zipCode),
                () -> "Invalid ZIP code [" + zipCode + "] should normalize to null");
    }

    @Test
    @DisplayName("Given blank zip, when create, then throws IllegalArgumentException")
    void givenBlankZip_whenCreate_thenThrowsIllegalArgumentException() {
        final var exception = assertThrows(
                IllegalArgumentException.class,
                () -> new ZipCodeAddress(" ", "Rua Augusta", "Centro", "São Paulo", "SP", "Brasil"),
                () -> "Creating ZipCodeAddress with blank zipCode should throw IllegalArgumentException");

        assertEquals(
                "'zipCode' should not be null or blank",
                exception.getMessage(),
                () -> "Exception message should indicate that zipCode must not be null or blank");
    }
}
