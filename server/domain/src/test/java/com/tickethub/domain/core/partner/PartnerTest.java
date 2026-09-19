package com.tickethub.domain.core.partner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

class PartnerTest {

    private static final String VALID_EMAIL = "cinema@domain.com";
    private static final String VALID_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "J"})
    void givenInvalidName_whenCreate_thenThrowDomainExceptionForNullAndBlank(String name) {
        assertEquals("Invalid name " + name, assertThrows(DomainException.class,
                () -> Partner.create(name, "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, VALID_HASH)).getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123"})
    void givenInvalidCnpj_whenCreate_thenThrowDomainExceptionForNullAndBlank(String cnpj) {
        assertEquals("Invalid CNPJ", assertThrows(DomainException.class,
                () -> Partner.create("Cinema Nova", cnpj, com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, VALID_HASH)).getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plainaddress"})
    void givenInvalidEmail_whenCreate_thenThrowDomainException(String email) {
        assertEquals("Invalid email", assertThrows(DomainException.class,
                () -> Partner.create("Cinema Nova", "11222333000181", com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), email, VALID_HASH)).getMessage());
    }

    @Test
    void givenValidParams_whenCreate_thenInstantiatePartner() {
        final var expectedName = "Cinema Nova";
        final var expectedCnpj = "11222333000181";

        final var actualPartner = Partner.create(expectedName, expectedCnpj, com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, VALID_HASH);

        assertNotNull(actualPartner);
        assertNotNull(actualPartner.getId());
        assertEquals(expectedName, actualPartner.getName().getValue());
        assertEquals(expectedCnpj, actualPartner.getCnpj().getValue());
        assertEquals(VALID_EMAIL, actualPartner.getEmail().getValue());
        assertEquals(VALID_HASH, actualPartner.getPasswordHash().getValue());
    }

    @Test
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedName = "J";
        final var expectedCnpj = "11222333000181";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Partner.create(expectedName, expectedCnpj, com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, VALID_HASH)
        );

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }

    @Test
    void givenInvalidCnpj_whenCreate_thenThrowDomainException() {
        final var expectedName = "Cinema Nova";
        final var expectedCnpj = "11222333000182";

        DomainException exception = assertThrows(
                DomainException.class,
                () -> Partner.create(expectedName, expectedCnpj, com.tickethub.domain.shared.Address.create("Rua A", "10", null, "Centro", "Sao Paulo", "SP", "Brasil", "01001000"), VALID_EMAIL, VALID_HASH)
        );

        assertEquals("Invalid CNPJ", exception.getMessage());
    }
}
