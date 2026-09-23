package com.tickethub.domain.core.customer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Customer")
class CustomerTest {

    private static final String VALID_EMAIL = "john@domain.com";
    private static final String VALID_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "123", "12345678900"})
    @DisplayName("Given invalid CPF, when create, then throw domain exception")
    void givenInvalidCpf_whenCreate_thenThrowDomainException(String cpf) {
        assertEquals(
                "Invalid CPF",
                assertThrows(DomainException.class, () -> Customer.create(cpf, "John Doe", VALID_EMAIL, VALID_HASH))
                        .getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "J", "John123"})
    @DisplayName("Given invalid name, when create, then throw domain exception")
    void givenInvalidName_whenCreate_thenThrowDomainException(String name) {
        assertEquals(
                "Invalid name " + name,
                assertThrows(DomainException.class, () -> Customer.create("12345678909", name, VALID_EMAIL, VALID_HASH))
                        .getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plainaddress", "missing-domain@"})
    @DisplayName("Given invalid email, when create, then throw domain exception")
    void givenInvalidEmail_whenCreate_thenThrowDomainException(String email) {
        assertEquals(
                "Invalid email",
                assertThrows(DomainException.class, () -> Customer.create("12345678909", "John Doe", email, VALID_HASH))
                        .getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "plain-password"})
    @DisplayName("Given invalid password hash, when create, then throw domain exception")
    void givenInvalidPasswordHash_whenCreate_thenThrowDomainException(String passwordHash) {
        assertEquals(
                "Invalid password hash",
                assertThrows(
                                DomainException.class,
                                () -> Customer.create("12345678909", "John Doe", VALID_EMAIL, passwordHash))
                        .getMessage());
    }

    @Test
    @DisplayName("Given valid params, when create, then instantiate customer")
    void givenValidParams_whenCreate_thenInstantiateCustomer() {
        final var expectedCpf = "12345678909";
        final var expectedName = "John Doe";

        final var actualCustomer = Customer.create(expectedCpf, expectedName, VALID_EMAIL, VALID_HASH);

        assertNotNull(actualCustomer);
        assertNotNull(actualCustomer.getId());
        assertEquals(expectedCpf, actualCustomer.getCpf().getValue());
        assertEquals(expectedName, actualCustomer.getName().getValue());
        assertEquals(VALID_EMAIL, actualCustomer.getEmail().getValue());
        assertEquals(VALID_HASH, actualCustomer.getPasswordHash().getValue());
    }

    @Test
    @DisplayName("Given invalid CPF, when create, then throw domain exception")
    void givenInvalidCpf_whenCreate_thenThrowDomainException() {
        final var expectedCpf = "12345678900";
        final var expectedName = "John Doe";

        DomainException exception = assertThrows(
                DomainException.class, () -> Customer.create(expectedCpf, expectedName, VALID_EMAIL, VALID_HASH));

        assertEquals("Invalid CPF", exception.getMessage());
    }

    @Test
    @DisplayName("Given invalid name, when create, then throw domain exception")
    void givenInvalidName_whenCreate_thenThrowDomainException() {
        final var expectedCpf = "12345678909";
        final var expectedName = "J";

        DomainException exception = assertThrows(
                DomainException.class, () -> Customer.create(expectedCpf, expectedName, VALID_EMAIL, VALID_HASH));

        assertEquals("Invalid name " + expectedName, exception.getMessage());
    }

    @Test
    @DisplayName("Given valid customer, when change email, then update email")
    void givenValidCustomer_whenChangeEmail_thenUpdateEmail() {
        final var customer = Customer.create("12345678909", "John Doe", VALID_EMAIL, VALID_HASH);

        customer.changeEmail("new@domain.com");

        assertEquals("new@domain.com", customer.getEmail().getValue());
    }

    @Test
    @DisplayName("Given valid customer, when change password, then update password hash")
    void givenValidCustomer_whenChangePassword_thenUpdatePasswordHash() {
        final var customer = Customer.create("12345678909", "John Doe", VALID_EMAIL, VALID_HASH);
        final var expectedHash = "$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2";

        customer.changePassword(expectedHash);

        assertEquals(expectedHash, customer.getPasswordHash().getValue());
    }
}
