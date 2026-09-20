package com.tickethub.application.authentication.logout;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tickethub.domain.authentication.RefreshSession;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.SecureTokens;

@ExtendWith(MockitoExtension.class)
@DisplayName("Logout use case")
class LogoutUseCaseTest {

    @Mock
    private RefreshSessionGateway refreshSessions;

    private DefaultLogoutUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefaultLogoutUseCase(refreshSessions);
    }

    @Test
    @DisplayName("Given known token, when execute, then revokes session")
    void givenKnownToken_whenExecute_thenRevokesSession() {
        final var presented = SecureTokens.generateOpaqueToken();
        final var session = RefreshSession.issue(SecureTokens.sha256Hex(presented), "maria@domain.com",
                List.of(), null, Duration.ofDays(7));
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
}
