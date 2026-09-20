package com.tickethub.domain.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("CepAddress")
class CepAddressTest {

    @Test
    @DisplayName("Given formatted zip, when normalize, then returns only digits")
    void givenFormattedZip_whenNormalize_thenReturnsDigits() {
        assertEquals("01305000", CepAddress.normalize("01305-000"),
                () -> "Normalized CEP should strip formatting and keep only digits");
    }

    @Test
    @DisplayName("Given digits-only zip, when normalize, then returns same digits")
    void givenDigitsOnlyZip_whenNormalize_thenReturnsDigits() {
        assertEquals("01305000", CepAddress.normalize("01305000"),
                () -> "Normalized CEP should preserve digits-only input");
    }

    @ParameterizedTest(name = "Given invalid zip \"{0}\", when normalize, then returns null")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "123456789", "abcdefgh"})
    @DisplayName("Given invalid zip, when normalize, then returns null")
    void givenInvalidZip_whenNormalize_thenReturnsNull(String zipCode) {
        assertEquals(null, CepAddress.normalize(zipCode),
                () -> "Invalid CEP [" + zipCode + "] should normalize to null");
    }

    @Test
    @DisplayName("Given blank zip, when create, then throws IllegalArgumentException")
    void givenBlankZip_whenCreate_thenThrowsIllegalArgumentException() {
        final var exception = assertThrows(IllegalArgumentException.class,
                () -> new CepAddress(" ", "Rua Augusta", "Centro", "São Paulo", "SP", "Brasil"),
                () -> "Creating CepAddress with blank zipCode should throw IllegalArgumentException");

        assertEquals("'zipCode' should not be null or blank", exception.getMessage(),
                () -> "Exception message should indicate that zipCode must not be null or blank");
    }
}
