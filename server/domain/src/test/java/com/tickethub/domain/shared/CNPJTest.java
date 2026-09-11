package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class CNPJTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "abcdefghijklmn"})
    void givenNullEmptyOrMalformedCnpj_whenCreate_thenThrowDomainException(String value) {
        assertEquals("Invalid CNPJ", assertThrows(DomainException.class,
                () -> CNPJ.create(value)).getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000181", "04252011000110", "11444777000161", "12ABC34501DE35"})
    void givenAValidCNPJ_whenCreate_thenStoreValue(String value) {
        assertEquals(value, CNPJ.create(value).getValue());
    }

    @Test
    void givenAValidFormattedCNPJ_whenCreate_thenStoreOnlyDigits() {
        CNPJ cnpj = CNPJ.create("11.222.333/0001-81");

        assertEquals("11222333000181", cnpj.getValue());
    }

    @Test
    void givenAFormattedAlphanumericCNPJ_whenCreate_thenPreserveLetters() {
        CNPJ cnpj = CNPJ.create("12.ABC.345/01DE-35");

        assertEquals("12ABC34501DE35", cnpj.getValue());
    }

    @Test
    void givenAFormattedCNPJWithSpaces_whenCreate_thenNormalizeValue() {
        assertEquals("04252011000110", CNPJ.create("  04.252.011/0001-10  ").getValue());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "./-", "1122233300018", "112223330001810"})
    void givenACNPJWithInvalidLength_whenCreate_thenThrowDomainException(String value) {
        DomainException exception = assertThrows(DomainException.class, () -> CNPJ.create(value));

        assertEquals("Invalid CNPJ", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000000", "11111111111111", "22222222222222", "33333333333333",
            "44444444444444", "55555555555555", "66666666666666", "77777777777777",
            "88888888888888", "99999999999999"
    })
    void givenACNPJWithAllDigitsEqual_whenCreate_thenThrowDomainException(String value) {
        assertThrows(DomainException.class, () -> CNPJ.create(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"11222333000191", "11222333000182", "12ABC34501DE45", "12ABC34501DE36"})
    void givenACNPJWithInvalidCheckDigits_whenCreate_thenThrowDomainException(String value) {
        assertThrows(DomainException.class, () -> CNPJ.create(value));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1122233300018A", "12ABC34501DE3A", "@11222333000181", "11222333000181!", "12@BC34501DE35"})
    void givenACNPJWithInvalidCharacters_whenCreate_thenThrowDomainException(String value) {
        assertThrows(DomainException.class, () -> CNPJ.create(value));
    }

    @Test
    void givenAValidCNPJ_whenToString_thenReturnFormattedCNPJ() {
        assertEquals("11.222.333/0001-81", CNPJ.create("11222333000181").toString());
    }

    @Test
    void givenAnAlphanumericCNPJ_whenToString_thenReturnFormattedCNPJ() {
        assertEquals("12.ABC.345/01DE-35", CNPJ.create("12ABC34501DE35").toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"11.222.333/0001-81", "12.ABC.345/01DE-35"})
    void givenFormattedAndUnformattedSameCNPJ_whenCompare_thenBeEqual(String value) {
        CNPJ first = CNPJ.create(value);
        CNPJ second = CNPJ.create(first.getValue());

        assertEquals(first, second);
        assertEquals(second, first);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenDifferentCNPJs_whenCompare_thenNotBeEqual() {
        assertNotEquals(CNPJ.create("11222333000181"), CNPJ.create("04252011000110"));
    }

    @Test
    void givenANullOrDifferentType_whenCompare_thenNotBeEqual() {
        CNPJ cnpj = CNPJ.create("11222333000181");

        assertFalse(cnpj.equals(null));
        assertFalse(cnpj.equals(cnpj.getValue()));
    }
}
