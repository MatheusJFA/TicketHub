package com.tickethub.domain.authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tickethub.domain.exception.DomainException;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RefreshSession")
class RefreshSessionTest {

    private static final List<String> AUTHORITIES = List.of("ROLE_CUSTOMER", "customer:write");

    @Test
    @DisplayName("Given valid params, when issue, then creates active session")
    void givenValidParams_whenIssue_thenCreatesActiveSession() {
        final var session =
                RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, "owner-1", Duration.ofDays(7));

        assertNotNull(session.getId(), () -> "Issued session should have an id");
        assertNotNull(session.getFamilyId(), () -> "Issued session should have a family id");
        assertEquals("hash-1", session.getTokenHash(), () -> "Issued session should keep the given token hash");
        assertEquals("maria@domain.com", session.getSubject(), () -> "Issued session should keep the given subject");
        assertEquals(AUTHORITIES, session.getAuthorities(), () -> "Issued session should keep the given authorities");
        assertEquals("owner-1", session.getOwnerId(), () -> "Issued session should keep the given owner id");
        assertTrue(session.isActive(), () -> "Freshly issued session should be active");
        assertFalse(session.isRotated(), () -> "Freshly issued session should not be rotated");
        assertFalse(session.isRevoked(), () -> "Freshly issued session should not be revoked");
    }

    @Test
    @DisplayName("Given active session, when rotate, then links old to new in same family")
    void givenActiveSession_whenRotate_thenLinksOldToNewInSameFamily() {
        final var current = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null, Duration.ofDays(7));

        final var next = current.rotate("hash-2", Duration.ofDays(7));

        assertTrue(current.isRotated(), () -> "Rotated session should be marked as rotated");
        assertEquals(
                "hash-2",
                current.getReplacedByTokenHash(),
                () -> "Rotated session should link to the replacement token hash");
        assertEquals(
                current.getFamilyId(),
                next.getFamilyId(),
                () -> "Rotated and replacement sessions should share the same family id");
        assertEquals("hash-2", next.getTokenHash(), () -> "Replacement session should carry the new token hash");
        assertNotEquals(current.getId(), next.getId(), () -> "Replacement session should have a new id");
        assertTrue(next.isActive(), () -> "Replacement session should be active");
    }

    @Test
    @DisplayName("Given revoked session, when rotate, then throws DomainException")
    void givenRevokedSession_whenRotate_thenThrowDomainException() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null, Duration.ofDays(7));
        session.revoke();

        final var exception = assertThrows(
                DomainException.class,
                () -> session.rotate("hash-2", Duration.ofDays(7)),
                () -> "Rotating a revoked session should throw DomainException");

        assertEquals(
                "Refresh session is revoked",
                exception.getMessage(),
                () -> "Exception message should indicate that the refresh session is revoked");
    }

    @Test
    @DisplayName("Given rotated session, when rotate again, then throws DomainException")
    void givenRotatedSession_whenRotateAgain_thenThrowDomainException() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null, Duration.ofDays(7));
        session.rotate("hash-2", Duration.ofDays(7));

        final var exception = assertThrows(
                DomainException.class,
                () -> session.rotate("hash-3", Duration.ofDays(7)),
                () -> "Rotating an already rotated session should throw DomainException");

        assertEquals(
                "Refresh session was already rotated",
                exception.getMessage(),
                () -> "Exception message should indicate that the refresh session was already rotated");
    }

    @Test
    @DisplayName("Given revoked session, when is active, then returns false")
    void givenRevokedSession_whenIsActive_thenReturnsFalse() {
        final var session = RefreshSession.issue("hash-1", "maria@domain.com", AUTHORITIES, null, Duration.ofDays(7));
        session.revoke();

        assertTrue(session.isRevoked(), () -> "Revoked session should report as revoked");
        assertFalse(session.isActive(), () -> "Revoked session should not be active");
    }
}
