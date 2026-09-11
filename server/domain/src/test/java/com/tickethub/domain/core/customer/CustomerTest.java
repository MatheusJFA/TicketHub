package com.tickethub.domain.core.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

class CustomerTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "12345678900"})
    void givenInvalidCpf_whenCreate_thenThrowDomainException(String cpf) {
        assertEquals("Invalid CPF", assertThrows(DomainException.class,
                () -> Customer.create(cpf, "John Doe")).getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "J", "John123"})
    void givenInvalidName_whenCreate_thenThrowDomainException(String name) {
        assertEquals("Invalid name " + name, assertThrows(DomainException.class,
                () -> Customer.create("12345678909", name)).getMessage());
    }

    @Test
    void givenValidParams_whenCreate_thenInstantiateCustomer() {
        final var expectedCpf = "12345678909";
        final var expectedName = "John Doe";

        final var actualCustomer = Customer.create(expectedCpf, expectedName);

        assertNotNull(actualCustomer);
        assertNotNull(actualCustomer.getId());
        assertEquals(expectedCpf, actualCustomer.getCpf().getValue());
        assertEquals(expectedName, actualCustomer.getName().getValue());
    }

    @Test
    void givenInvalidCpf_whenCreate_thenThrowDomainException() {
        final var expectedCpf = "12345678900";
        final var expectedName = "John Doe";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Customer.create(expectedCpf, expectedName)
        );

        assertEquals("Invalid CPF", exception.getMessage());
    }

    @Test
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedCpf = "12345678909";
        final var expectedName = "J";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Customer.create(expectedCpf, expectedName)
        );

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }
}
