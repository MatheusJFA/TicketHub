package com.tickethub.domain.geo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class CepAddressTest {

    @Test
    void givenFormattedZip_whenNormalize_thenReturnsDigits() {
        assertEquals("01305000", CepAddress.normalize("01305-000"));
    }

    @Test
    void givenDigitsOnlyZip_whenNormalize_thenReturnsDigits() {
        assertEquals("01305000", CepAddress.normalize("01305000"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "123456789", "abcdefgh"})
    void givenInvalidZip_whenNormalize_thenReturnsNull(String zipCode) {
        assertEquals(null, CepAddress.normalize(zipCode));
    }

    @Test
    void givenBlankZip_whenCreate_thenThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
                () -> new CepAddress(" ", "Rua Augusta", "Centro", "São Paulo", "SP", "Brasil"));
    }
}
