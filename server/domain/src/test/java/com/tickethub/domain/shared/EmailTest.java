package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.tickethub.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Email")
class EmailTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {
                " ",
                "plainaddress",
                "missing-at-sign.com",
                "@missing-local.com",
                "missing-domain@",
                "missing-tld@domain",
                "double..dot@domain.com",
                ".leading-dot@domain.com",
                "trailing-dot.@domain.com",
                "user@.leading-dot.com",
                "user@domain..com"
            })
    @DisplayName("Given null empty or malformed email, when create, then throw domain exception")
    void givenNullEmptyOrMalformedEmail_whenCreate_thenThrowDomainException(String value) {
        assertEquals(
                "Invalid email",
                assertThrows(DomainException.class, () -> Email.create(value)).getMessage());
    }

    @Test
    @DisplayName("Given valid email, when create, then store value")
    void givenValidEmail_whenCreate_thenStoreValue() {
        assertEquals("user@domain.com", Email.create("user@domain.com").getValue());
    }

    @Test
    @DisplayName("Given uppercase email, when create, then store lowercased")
    void givenUppercaseEmail_whenCreate_thenStoreLowercased() {
        assertEquals("user@domain.com", Email.create("User@Domain.COM").getValue());
    }

    @Test
    @DisplayName("Given email with surrounding spaces, when create, then store trimmed")
    void givenEmailWithSurroundingSpaces_whenCreate_thenStoreTrimmed() {
        assertEquals("user@domain.com", Email.create("  user@domain.com  ").getValue());
    }

    @Test
    @DisplayName("Given same email with different case, when compare, then be equal")
    void givenSameEmailWithDifferentCase_whenCompare_thenBeEqual() {
        final Email first = Email.create("user@domain.com");
        final Email second = Email.create("USER@DOMAIN.COM");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Given different emails, when compare, then not be equal")
    void givenDifferentEmails_whenCompare_thenNotBeEqual() {
        assertNotEquals(Email.create("user@domain.com"), Email.create("other@domain.com"));
    }
}
