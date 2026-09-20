package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class AuditSanitizerTest {

    @Test
    void givenNull_whenSanitize_thenReturnsEmpty() {
        assertEquals(Optional.empty(), AuditSanitizer.sanitize(null));
    }

    @Test
    void givenPlainInput_whenSanitize_thenKeepsUnchanged() {
        assertEquals(Optional.of("CreateSpotCommand[A1]"),
                AuditSanitizer.sanitize("CreateSpotCommand[A1]"));
    }

    @Test
    void givenEqualsNotation_whenSanitize_thenMasksPassword() {
        assertEquals(Optional.of("CreateCustomerCommand[password=***]"),
                AuditSanitizer.sanitize("CreateCustomerCommand[password=secret-123]"));
    }

    @Test
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
    void givenMixedCaseKey_whenSanitize_thenMasks() {
        assertEquals(Optional.of("[Password=***]"),
                AuditSanitizer.sanitize("[Password=hunter2]"));
    }

    @Test
    void givenLongInput_whenSanitize_thenTruncates() {
        final var sanitized = AuditSanitizer.sanitize("x".repeat(5000)).orElseThrow();

        assertTrue(sanitized.endsWith("...[truncated]"));
        assertEquals(AuditSanitizer.MAX_INPUT_LENGTH + "...[truncated]".length(), sanitized.length());
    }
}
