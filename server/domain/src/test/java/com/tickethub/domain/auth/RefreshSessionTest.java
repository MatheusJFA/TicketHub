package com.tickethub.domain.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.tickethub.domain.exception.DomainException;

class RefreshSessionTest {

    private static final List<String> AUTHORITIES = List.of("ROLE_CUSTOMER", "customer:write");

    @Test
    void givenValidParams_whenIssue_thenCreatesActiveSession() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, "owner-1",
                Duration.ofDays(7));

        assertNotNull(session.getId());
        assertNotNull(session.getFamilyId());
        assertEquals("hash-1", session.getTokenHash());
        assertEquals("maria@domain.com", session.getSubject());
        assertEquals(AUTHORITIES, session.getAuthorities());
        assertEquals("owner-1", session.getOwnerId());
        assertTrue(session.isActive());
        assertFalse(session.isRotated());
        assertFalse(session.isRevoked());
    }

    @Test
    void givenActiveSession_whenRotate_thenLinksOldToNewInSameFamily() {
        final var current = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null,
                Duration.ofDays(7));

        final var next = current.rotate("hash-2", Duration.ofDays(7));

        assertTrue(current.isRotated());
        assertEquals("hash-2", current.getReplacedByTokenHash());
        assertEquals(current.getFamilyId(), next.getFamilyId());
        assertEquals("hash-2", next.getTokenHash());
        assertNotEquals(current.getId(), next.getId());
        assertTrue(next.isActive());
    }

    @Test
    void givenRevokedSession_whenRotate_thenThrowDomainException() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null,
                Duration.ofDays(7));
        session.revoke();

        assertThrows(DomainException.class, () -> session.rotate("hash-2", Duration.ofDays(7)));
    }

    @Test
    void givenRotatedSession_whenRotateAgain_thenThrowDomainException() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null,
                Duration.ofDays(7));
        session.rotate("hash-2", Duration.ofDays(7));

        assertThrows(DomainException.class, () -> session.rotate("hash-3", Duration.ofDays(7)));
    }

    @Test
    void givenRevokedSession_whenIsActive_thenReturnsFalse() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null,
                Duration.ofDays(7));
        session.revoke();

        assertTrue(session.isRevoked());
        assertFalse(session.isActive());
    }
}
