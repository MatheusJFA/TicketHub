package com.tickethub.application.authentication.refresh;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.DisplayName;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tickethub.domain.authentication.IssuedToken;
import com.tickethub.domain.authentication.RefreshSession;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.SecureTokens;
import com.tickethub.domain.authentication.TokenIssuer;

@ExtendWith(MockitoExtension.class)
@DisplayName("Refresh token use case")
class RefreshTokenUseCaseTest {

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private RefreshSessionGateway refreshSessions;

    private DefaultRefreshTokenUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefaultRefreshTokenUseCase(tokenIssuer, refreshSessions, Duration.ofDays(7));
    }

    @Test
    @DisplayName("Given active session, when execute, then rotates and returns new tokens")
    void givenActiveSession_whenExecute_thenRotatesAndReturnsNewTokens() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(SecureTokens.sha256Hex(presented), "maria@domain.com",
                List.of("ROLE_CUSTOMER"), "customer-1", Duration.ofDays(7));
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));
        when(tokenIssuer.issueAccess("maria@domain.com", List.of("ROLE_CUSTOMER"), "customer-1"))
                .thenReturn(new IssuedToken("new-access", 900));

        final var output = useCase.execute(RefreshTokenCommand.with(presented)).getRight();

        assertEquals("new-access", output.accessToken());
        assertEquals("Bearer", output.tokenType());
        assertEquals(900, output.expiresIn());
        assertNotEquals(presented, output.refreshToken());
        assertTrue(session.isRotated());
        final var saved = ArgumentCaptor.forClass(RefreshSession.class);
        verify(refreshSessions, times(2)).save(saved.capture());
        assertTrue(saved.getAllValues().stream().anyMatch(found -> found.getTokenHash().equals(session.getTokenHash())));
    }

    @Test
    @DisplayName("Given unknown token, when execute, then returns401")
    void givenUnknownToken_whenExecute_thenReturns401() {
        when(refreshSessions.findByTokenHash(any())).thenReturn(Optional.empty());

        final var notification = useCase.execute(RefreshTokenCommand.with("unknown")).getLeft();

        assertEquals("Invalid refresh token", notification.firstError().message());
        verify(refreshSessions, never()).save(any());
        verify(tokenIssuer, never()).issueAccess(any(), any(), any());
    }

    @Test
    @DisplayName("Given revoked session, when execute, then returns401")
    void givenRevokedSession_whenExecute_thenReturns401() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(SecureTokens.sha256Hex(presented), "maria@domain.com",
                List.of(), null, Duration.ofDays(7));
        session.revoke();
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));

        final var notification = useCase.execute(RefreshTokenCommand.with(presented)).getLeft();

        assertEquals("Invalid refresh token", notification.firstError().message());
        verify(refreshSessions, never()).save(any());
    }

    @Test
    @DisplayName("Given rotated token reuse, when execute, then revokes family and returns401")
    void givenRotatedTokenReuse_whenExecute_thenRevokesFamilyAndReturns401() {
        final var first = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(SecureTokens.sha256Hex(first), "maria@domain.com",
                List.of(), null, Duration.ofDays(7));
        session.rotate(SecureTokens.sha256Hex(SecureTokens.generateOpaqueToken()), Duration.ofDays(7));
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));
        when(refreshSessions.findByFamilyId(session.getFamilyId())).thenReturn(List.of(session));

        final var notification = useCase.execute(RefreshTokenCommand.with(first)).getLeft();

        assertEquals("Invalid refresh token", notification.firstError().message());
        assertTrue(session.isRevoked());
        verify(refreshSessions, times(1)).save(session);
        verify(tokenIssuer, never()).issueAccess(any(), any(), any());
    }
}
