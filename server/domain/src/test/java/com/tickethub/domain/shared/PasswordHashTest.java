package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.tickethub.domain.exception.DomainException;

class PasswordHashTest {

    private static final String VALID_HASH = "$2a$10$yK7PogeVNyS8.guDq1yKneeynLO7jVthcXy5ZQonI6gid0M4kGhKS";

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "plain-password",
            "$2a$10$too-short",
            "$1$10$not-bcrypt-but-valid-crypt"
    })
    void givenNullEmptyOrMalformedHash_whenFromHash_thenThrowDomainException(String value) {
        assertEquals("Invalid password hash", assertThrows(DomainException.class,
                () -> PasswordHash.fromHash(value)).getMessage());
    }

    @Test
    void givenValidBcryptHash_whenFromHash_thenStoreValue() {
        assertEquals(VALID_HASH, PasswordHash.fromHash(VALID_HASH).getValue());
    }

    @Test
    void givenSameHash_whenCompare_thenBeEqual() {
        final PasswordHash first = PasswordHash.fromHash(VALID_HASH);
        final PasswordHash second = PasswordHash.fromHash(VALID_HASH);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void givenDifferentHashes_whenCompare_thenNotBeEqual() {
        assertNotEquals(
                PasswordHash.fromHash(VALID_HASH),
                PasswordHash.fromHash("$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2"));
    }

    @Test
    void givenPasswordHash_whenToString_thenDoNotExposeHash() {
        assertEquals("***", PasswordHash.fromHash(VALID_HASH).toString());
    }
}
