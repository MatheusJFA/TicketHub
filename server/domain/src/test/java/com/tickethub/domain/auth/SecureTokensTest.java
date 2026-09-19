package com.tickethub.domain.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SecureTokensTest {

    @Test
    void givenNoInput_whenGenerateOpaqueToken_thenReturnsUniqueUrlSafeTokens() {
        final var first = SecureTokens.generateOpaqueToken();
        final var second = SecureTokens.generateOpaqueToken();

        assertNotEquals(first, second);
        assertTrue(first.matches("[A-Za-z0-9_-]+"));
    }

    @Test
    void givenSameValue_whenSha256Hex_thenReturnsDeterministicDigest() {
        assertEquals(SecureTokens.sha256Hex("token"), SecureTokens.sha256Hex("token"));
        assertEquals(64, SecureTokens.sha256Hex("token").length());
    }
}
