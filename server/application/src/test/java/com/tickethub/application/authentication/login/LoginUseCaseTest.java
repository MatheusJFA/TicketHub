package com.tickethub.application.authentication.login;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.domain.authentication.AuthAccount;
import com.tickethub.domain.authentication.AuthAccountGateway;
import com.tickethub.domain.authentication.IssuedToken;
import com.tickethub.domain.authentication.PasswordHasher;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.TokenIssuer;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("Login use case")
class LoginUseCaseTest {

    private static final AuthAccount ACCOUNT =
            new AuthAccount("maria@domain.com", "hash", List.of("ROLE_CUSTOMER", "customer:write"), "customer-1");

    @Mock
    private AuthAccountGateway authAccounts;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenIssuer tokenIssuer;

    @Mock
    private RefreshSessionGateway refreshSessions;

    private DefaultLoginUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase =
                new DefaultLoginUseCase(authAccounts, passwordHasher, tokenIssuer, refreshSessions, Duration.ofDays(7));
    }

    @Test
    @DisplayName("Given valid credentials, when execute, then returns access and refresh tokens")
    void givenValidCredentials_whenExecute_thenReturnsAccessAndRefreshTokens() {
        when(authAccounts.findByIdentifier("maria@domain.com")).thenReturn(Optional.of(ACCOUNT));
        when(passwordHasher.matches("secret-123", "hash")).thenReturn(true);
        when(tokenIssuer.issueAccess("maria@domain.com", ACCOUNT.authorities(), "customer-1"))
                .thenReturn(new IssuedToken("access-token", 900));

        final var output = useCase.execute(LoginCommand.with("maria@domain.com", "secret-123"))
                .getRight();

        assertEquals("access-token", output.accessToken());
        assertEquals("Bearer", output.tokenType());
        assertEquals(900, output.expiresIn());
        assertTrue(isNotBlank(output.refreshToken()));
        verify(authAccounts, times(1)).findByIdentifier("maria@domain.com");
        verify(passwordHasher, times(1)).matches("secret-123", "hash");
        verify(tokenIssuer, times(1)).issueAccess("maria@domain.com", ACCOUNT.authorities(), "customer-1");
        verify(refreshSessions, times(1))
                .save(argThat(session -> session.getSubject().equals("maria@domain.com")
                        && session.getOwnerId().equals("customer-1")
                        && session.isActive()));
    }

    @Test
    @DisplayName("Given unknown identifier, when execute, then returns401 without leaking")
    void givenUnknownIdentifier_whenExecute_thenReturns401WithoutLeaking() {
        when(authAccounts.findByIdentifier("ghost@domain.com")).thenReturn(Optional.empty());

        final var notification = useCase.execute(LoginCommand.with("ghost@domain.com", "secret-123"))
                .getLeft();

        assertEquals(1, notification.getErrors().size());
        assertEquals("Invalid credentials", notification.firstError().message());
        verify(authAccounts, times(1)).findByIdentifier("ghost@domain.com");
        verify(passwordHasher, never()).matches(any(), any());
        verify(tokenIssuer, never()).issueAccess(any(), any(), any());
        verify(refreshSessions, never()).save(any());
    }

    @Test
    @DisplayName("Given wrong password, when execute, then returns401 without issuing tokens")
    void givenWrongPassword_whenExecute_thenReturns401WithoutIssuingTokens() {
        when(authAccounts.findByIdentifier("maria@domain.com")).thenReturn(Optional.of(ACCOUNT));
        when(passwordHasher.matches("wrong", "hash")).thenReturn(false);

        final var notification =
                useCase.execute(LoginCommand.with("maria@domain.com", "wrong")).getLeft();

        assertEquals("Invalid credentials", notification.firstError().message());
        verify(passwordHasher, times(1)).matches("wrong", "hash");
        verify(tokenIssuer, never()).issueAccess(any(), any(), any());
        verify(refreshSessions, never()).save(any());
    }

    @Test
    @DisplayName("Given issuer failure, when execute, then returns notification")
    void givenIssuerFailure_whenExecute_thenReturnsNotification() {
        when(authAccounts.findByIdentifier("maria@domain.com")).thenReturn(Optional.of(ACCOUNT));
        when(passwordHasher.matches("secret-123", "hash")).thenReturn(true);
        when(tokenIssuer.issueAccess(any(), any(), any())).thenThrow(new IllegalStateException("jwt failed"));

        final var notification = useCase.execute(LoginCommand.with("maria@domain.com", "secret-123"))
                .getLeft();

        assertEquals("jwt failed", notification.firstError().message());
        verify(refreshSessions, never()).save(any());
    }
}
