package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

public class NameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "J", "John123"})
    void givenNullEmptyBlankOrMalformedName_whenCreate_thenThrowDomainException(String value) {
        assertEquals("Invalid name " + value, assertThrows(DomainException.class,
                () -> Name.create(value)).getMessage());
    }
    @Test
    void givenAValidName_whenCreate_thenStoreValue() {
        assertEquals("João da Silva", Name.create("João da Silva").getValue());
    }

    @Test
    void givenANameWithHyphen_whenCreate_thenStoreValue() {
        assertEquals("Jean-Pierre", Name.create("Jean-Pierre").getValue());
    }

    @Test
    void givenANameWithApostrophe_whenCreate_thenStoreValue() {
        assertEquals("O'Connor", Name.create("O'Connor").getValue());
    }

    @Test
    void givenANullName_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Name.create(null));
    }

    @Test
    void givenABlankName_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Name.create("   "));
    }

    @Test
    void givenANameWithInvalidCharacters_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Name.create("John@Doe"));
    }

    @Test
    void givenANameBelowMinimumLength_whenCreate_thenThrowDomainException() {
        assertThrows(DomainException.class, () -> Name.create("A"));
    }

    @Test
    void givenAValidNameWithExtraSpaces_whenCreate_thenNormalizeName() {
        Name name = Name.create("   João    da Silva   ");

        assertEquals("João da Silva", name.getValue());
    }

    @Test
    void givenAnInvalidName_whenCreate_thenThrowDomainException() {
        DomainException exception = assertThrows(
                DomainException.class,
                () -> Name.create("John123")
        );
        assertEquals(DomainException.class, exception.getClass());
        assertEquals("Invalid name John123", exception.getMessage());
    }
}
