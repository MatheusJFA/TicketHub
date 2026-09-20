package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

@DisplayName("Name")
public class NameTest {

    @ParameterizedTest(name = "Given null, empty, blank or malformed name \"{0}\", when create, then throws DomainException")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "J", "John123"})
    @DisplayName("Given null, empty, blank or malformed name, when create, then throws DomainException")
    void givenNullEmptyBlankOrMalformedName_whenCreate_thenThrowDomainException(String value) {
        final var exception = assertThrows(DomainException.class,
                () -> Name.create(value),
                () -> "Creating Name with value [" + value + "] should throw DomainException");

        assertEquals("Invalid name " + value, exception.getMessage(),
                () -> "Exception message should be \"Invalid name " + value + "\"");
    }

    @Test
    @DisplayName("Given valid name, when create, then stores value")
    void givenAValidName_whenCreate_thenStoreValue() {
        assertEquals("João da Silva", Name.create("João da Silva").getValue(),
                () -> "Valid name should be stored as provided");
    }

    @Test
    @DisplayName("Given name with hyphen, when create, then stores value")
    void givenANameWithHyphen_whenCreate_thenStoreValue() {
        assertEquals("Jean-Pierre", Name.create("Jean-Pierre").getValue(),
                () -> "Name with hyphen should be stored as provided");
    }

    @Test
    @DisplayName("Given name with apostrophe, when create, then stores value")
    void givenANameWithApostrophe_whenCreate_thenStoreValue() {
        assertEquals("O'Connor", Name.create("O'Connor").getValue(),
                () -> "Name with apostrophe should be stored as provided");
    }

    @Test
    @DisplayName("Given null name, when create, then throws DomainException")
    void givenANullName_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> Name.create(null),
                () -> "Creating Name with null should throw DomainException");

        assertEquals("Invalid name null", exception.getMessage(),
                () -> "Exception message should be \"Invalid name null\"");
    }

    @Test
    @DisplayName("Given blank name, when create, then throws DomainException")
    void givenABlankName_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> Name.create("   "),
                () -> "Creating Name with blank value should throw DomainException");

        assertEquals("Invalid name    ", exception.getMessage(),
                () -> "Exception message should be \"Invalid name    \"");
    }

    @Test
    @DisplayName("Given name with invalid characters, when create, then throws DomainException")
    void givenANameWithInvalidCharacters_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> Name.create("John@Doe"),
                () -> "Creating Name with invalid characters should throw DomainException");

        assertEquals("Invalid name John@Doe", exception.getMessage(),
                () -> "Exception message should be \"Invalid name John@Doe\"");
    }

    @Test
    @DisplayName("Given name below minimum length, when create, then throws DomainException")
    void givenANameBelowMinimumLength_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(DomainException.class, () -> Name.create("A"),
                () -> "Creating Name below minimum length should throw DomainException");

        assertEquals("Invalid name A", exception.getMessage(),
                () -> "Exception message should be \"Invalid name A\"");
    }

    @Test
    @DisplayName("Given valid name with extra spaces, when create, then normalizes name")
    void givenAValidNameWithExtraSpaces_whenCreate_thenNormalizeName() {
        Name name = Name.create("   João    da Silva   ");

        assertEquals("João da Silva", name.getValue(),
                () -> "Name with extra spaces should be normalized");
    }

    @Test
    @DisplayName("Given invalid name, when create, then throws DomainException")
    void givenAnInvalidName_whenCreate_thenThrowDomainException() {
        final var exception = assertThrows(
                DomainException.class,
                () -> Name.create("John123"),
                () -> "Creating Name with invalid value should throw DomainException"
        );
        assertEquals(DomainException.class, exception.getClass(),
                () -> "Exception should be of type DomainException");
        assertEquals("Invalid name John123", exception.getMessage(),
                () -> "Exception message should be \"Invalid name John123\"");
    }
}
