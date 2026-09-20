package com.tickethub.domain.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Secure tokens")
class SecureTokensTest {

    @Test
    @DisplayName("Given no input, when generate opaque token, then returns unique url safe tokens")
    void givenNoInput_whenGenerateOpaqueToken_thenReturnsUniqueUrlSafeTokens() {
        final var first = SecureTokens.generateOpaqueToken();
        final var second = SecureTokens.generateOpaqueToken();

        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    @DisplayName("Given same value, when sha256 hex, then returns deterministic digest")
    void givenSameValue_whenSha256Hex_thenReturnsDeterministicDigest() {
        assertEquals(SecureTokens.sha256Hex("token"), SecureTokens.sha256Hex("token"));
        assertEquals(64, SecureTokens.sha256Hex("token").length());
    }
}
