package com.tickethub.domain.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.DisplayName;

import com.tickethub.domain.exception.DomainException;

@DisplayName("Password hash")
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
    @DisplayName("Given null empty or malformed hash, when from hash, then throw domain exception")
    void givenNullEmptyOrMalformedHash_whenFromHash_thenThrowDomainException(String value) {
        assertEquals("Invalid password hash", assertThrows(DomainException.class,
                () -> PasswordHash.fromHash(value)).getMessage());
    }

    @Test
    @DisplayName("Given valid bcrypt hash, when from hash, then store value")
    void givenValidBcryptHash_whenFromHash_thenStoreValue() {
        assertEquals(VALID_HASH, PasswordHash.fromHash(VALID_HASH).getValue());
    }

    @Test
    @DisplayName("Given same hash, when compare, then be equal")
    void givenSameHash_whenCompare_thenBeEqual() {
        final PasswordHash first = PasswordHash.fromHash(VALID_HASH);
        final PasswordHash second = PasswordHash.fromHash(VALID_HASH);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    @DisplayName("Given different hashes, when compare, then not be equal")
    void givenDifferentHashes_whenCompare_thenNotBeEqual() {
        assertNotEquals(
                PasswordHash.fromHash(VALID_HASH),
                PasswordHash.fromHash("$2a$10$8oOcWuB1hV9DFQk73vuOr.4NE22eOqPObY/YeQBli2zYPuo4he2d2"));
    }

    @Test
    @DisplayName("Given password hash, when to string, then mask without exposing hash")
    void givenPasswordHash_whenToString_thenMaskWithoutExposingHash() {
        final var masked = PasswordHash.fromHash(VALID_HASH).toString();

        assertEquals("*".repeat(100), masked,
                () -> "toString should mask the hash with a fixed-length asterisk mask");
        assertFalse(masked.contains(VALID_HASH),
                () -> "toString should not expose the hash value");
    }
}
