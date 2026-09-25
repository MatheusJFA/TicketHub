package com.tickethub.application.authentication.logout;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.domain.authentication.AccessTokenIdentity;
import com.tickethub.domain.authentication.AccessTokenInspector;
import com.tickethub.domain.authentication.RefreshSession;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import com.tickethub.domain.authentication.SecureTokens;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Logout use case")
class LogoutUseCaseTest {

    @Mock
    private RefreshSessionGateway refreshSessions;

    @Mock
    private RevokedAccessTokenGateway revokedAccessTokens;

    @Mock
    private AccessTokenInspector accessTokenInspector;

    private DefaultLogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefaultLogoutUseCase(refreshSessions, revokedAccessTokens, accessTokenInspector);
    }

    @Test
    @DisplayName("Given known token, when execute, then revokes session")
    void givenKnownToken_whenExecute_thenRevokesSession() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(
                SecureTokens.sha256Hex(presented), "maria@domain.com", List.of(), null, Duration.ofDays(7));
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));

        final var result = useCase.execute(LogoutCommand.with(presented));

        assertTrue(result.isEmpty());
        assertTrue(session.isRevoked());
        verify(refreshSessions, times(1)).save(session);
    }

    @Test
    @DisplayName("Given unknown token, when execute, then succeeds without persistence")
    void givenUnknownToken_whenExecute_thenSucceedsWithoutPersistence() {
        when(refreshSessions.findByTokenHash(any())).thenReturn(Optional.empty());

        final var result = useCase.execute(LogoutCommand.with("unknown"));

        assertTrue(result.isEmpty());
        verify(refreshSessions, never()).save(any());
    }

    @Test
    @DisplayName("Given access token, when execute, then revokes access token id")
    void givenAccessToken_whenExecute_thenRevokesAccessTokenId() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(
                SecureTokens.sha256Hex(presented), "maria@domain.com", List.of(), null, Duration.ofDays(7));
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));
        final var identity = new AccessTokenIdentity("jti-1", Instant.now().plus(Duration.ofMinutes(30)));
        when(accessTokenInspector.inspect("access-1")).thenReturn(Optional.of(identity));

        final var result = useCase.execute(LogoutCommand.with(presented, "access-1"));

        assertTrue(result.isEmpty());
        verify(revokedAccessTokens, times(1)).revoke("jti-1", identity.expiresAt());
    }

    @Test
    @DisplayName("Given invalid access token, when execute, then still revokes refresh")
    void givenInvalidAccessToken_whenExecute_thenStillRevokesRefresh() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(
                SecureTokens.sha256Hex(presented), "maria@domain.com", List.of(), null, Duration.ofDays(7));
        when(refreshSessions.findByTokenHash(session.getTokenHash())).thenReturn(Optional.of(session));
        when(accessTokenInspector.inspect("bad-access")).thenReturn(Optional.empty());

        final var result = useCase.execute(LogoutCommand.with(presented, "bad-access"));

        assertTrue(result.isEmpty());
        assertTrue(session.isRevoked());
        verify(revokedAccessTokens, never()).revoke(any(), any());
    }
}
