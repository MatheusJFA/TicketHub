package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

@DisplayName("Audit sanitizer")
class AuditSanitizerTest {

    @Test
    @DisplayName("Given null, when sanitize, then returns empty")
    void givenNull_whenSanitize_thenReturnsEmpty() {
        assertEquals(Optional.empty(), AuditSanitizer.sanitize(null));
    }

    @Test
    @DisplayName("Given plain input, when sanitize, then keeps unchanged")
    void givenPlainInput_whenSanitize_thenKeepsUnchanged() {
        assertEquals(Optional.of("CreateSpotCommand[A1]"),
                AuditSanitizer.sanitize("CreateSpotCommand[A1]"));
    }

    @Test
    @DisplayName("Given equals notation, when sanitize, then masks password")
    void givenEqualsNotation_whenSanitize_thenMasksPassword() {
        assertEquals(Optional.of("CreateCustomerCommand[password=***]"),
                AuditSanitizer.sanitize("CreateCustomerCommand[password=secret-123]"));
    }

    @Test
    @DisplayName("Given json notation, when sanitize, then masks password and tokens")
    void givenJsonNotation_whenSanitize_thenMasksPasswordAndTokens() {
        final var sanitized = AuditSanitizer.sanitize(
                "{\"identifier\":\"admin\",\"password\":\"admin-local\",\"refreshToken\":\"abc.def.ghi\"}")
                .orElseThrow();

        assertTrue(sanitized.contains("\"password\":\"***\""), sanitized);
        assertTrue(sanitized.contains("\"refreshToken\":\"***\""), sanitized);
        assertTrue(sanitized.contains("\"identifier\":\"admin\""), sanitized);
        assertFalse(sanitized.contains("admin-local"), sanitized);
    }

    @Test
    @DisplayName("Given mixed case key, when sanitize, then masks")
    void givenMixedCaseKey_whenSanitize_thenMasks() {
        assertEquals(Optional.of("[Password=***]"),
                AuditSanitizer.sanitize("[Password=hunter2]"));
    }

    @Test
    @DisplayName("Given long input, when sanitize, then truncates")
    void givenLongInput_whenSanitize_thenTruncates() {
        final var sanitized = AuditSanitizer.sanitize("x".repeat(5000)).orElseThrow();

        assertTrue(sanitized.endsWith("...[truncated]"));
        assertEquals(AuditSanitizer.MAX_INPUT_LENGTH + "...[truncated]".length(), sanitized.length());
    }
}
