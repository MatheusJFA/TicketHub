package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

class EmailTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
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
    void givenNullEmptyOrMalformedEmail_whenCreate_thenThrowDomainException(String value) {
        assertEquals("Invalid email", assertThrows(DomainException.class,
                () -> Email.create(value)).getMessage());
    }

    @Test
    void givenValidEmail_whenCreate_thenStoreValue() {
        assertEquals("user@domain.com", Email.create("user@domain.com").getValue());
    }

    @Test
    void givenUppercaseEmail_whenCreate_thenStoreLowercased() {
        assertEquals("user@domain.com", Email.create("User@Domain.COM").getValue());
    }

    @Test
    void givenEmailWithSurroundingSpaces_whenCreate_thenStoreTrimmed() {
        assertEquals("user@domain.com", Email.create("  user@domain.com  ").getValue());
    }

    @Test
    void givenSameEmailWithDifferentCase_whenCompare_thenBeEqual() {
        final Email first = Email.create("user@domain.com");
        final Email second = Email.create("USER@DOMAIN.COM");

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenDifferentEmails_whenCompare_thenNotBeEqual() {
        assertNotEquals(Email.create("user@domain.com"), Email.create("other@domain.com"));
    }
}
