package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class CPFTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "abcdefghijk"})
    void givenNullEmptyOrMalformedCpf_whenCreate_thenThrowDomainException(String value) {
        assertEquals("Invalid CPF", assertThrows(DomainException.class,
                () -> CPF.create(value)).getMessage());
    }
    @Test
    void givenAValidCPF_whenCreate_thenStoreValue() {
        assertEquals("12345678909", CPF.create("12345678909").getValue());
    }

    @Test
    void givenACPFWithAllDigitsEqual_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> CPF.create("11111111111"));
    }

    @Test
    void givenACPFWithInvalidLength_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> CPF.create("1234567890"));
    }

    @Test
    void givenAValidFormattedCPF_whenCreate_thenStoreOnlyDigits() {
        CPF cpf = CPF.create("123.456.789-09");

        assertEquals("12345678909", cpf.getValue());
    }

    @Test
    void givenAValidCPF_whenToString_thenReturnFormattedCPF() {
        CPF cpf = CPF.create("12345678909");

        assertEquals("123.456.789-09", cpf.toString());
    }

    @Test
    void givenAnInvalidCPF_whenCreate_thenThrowDomainException() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> CPF.create("12345678900")
        );
        assertEquals(DomainException.class, exception.getClass());
        assertEquals("Invalid CPF", exception.getMessage());
    }

    @Test
    void givenANullCPF_whenCreate_thenThrowDomainException() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> CPF.create(null)
        );
        assertEquals(DomainException.class, exception.getClass());
    }

    @Test
    void givenFormattedAndUnformattedSameCPF_whenCompare_thenBeEqual() {
        CPF first = CPF.create("12345678909");
        CPF second = CPF.create("123.456.789-09");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenDifferentCPFs_whenCompare_thenNotBeEqual() {
        CPF first = CPF.create("12345678909");
        CPF second = CPF.create("52998224725");

        assertNotEquals(first, second);
    }
}
