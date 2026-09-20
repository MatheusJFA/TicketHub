package com.tickethub.application.auth.login;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.time.Duration;
import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.auth.AuthAccountGateway;
import com.tickethub.domain.auth.AuthenticationException;
import com.tickethub.domain.auth.PasswordHasher;
import com.tickethub.domain.auth.RefreshSession;
import com.tickethub.domain.auth.RefreshSessionGateway;
import com.tickethub.domain.auth.SecureTokens;
import com.tickethub.domain.auth.TokenIssuer;
import com.tickethub.domain.validation.Notification;

public class DefaultLoginUseCase extends LoginUseCase {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";

    private final AuthAccountGateway authAccounts;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final RefreshSessionGateway refreshSessions;
    private final Duration refreshTtl;

    public DefaultLoginUseCase(final AuthAccountGateway authAccounts, final PasswordHasher passwordHasher,
            final TokenIssuer tokenIssuer, final RefreshSessionGateway refreshSessions, final Duration refreshTtl) {
        this.authAccounts = Objects.requireNonNull(authAccounts);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer);
        this.refreshSessions = Objects.requireNonNull(refreshSessions);
        this.refreshTtl = isNull(refreshTtl) ? RefreshSession.DEFAULT_TTL : refreshTtl;
    }

    @Override
    public Either<Notification, LoginOutput> execute(final LoginCommand command) {
        try {
            final var identifier = defaultString(command.identifier()).trim();
            final var account = authAccounts.findByIdentifier(identifier).orElse(null);
            if (isNull(account)
                    || !passwordHasher.matches(defaultString(command.password()), account.passwordHash())) {
                return Either.left(unauthorized());
            }
            final var access = tokenIssuer.issueAccess(account.subject(), account.authorities(), account.ownerId());
            final var refreshToken = SecureTokens.generateOpaqueToken();
            refreshSessions.save(RefreshSession.issue(SecureTokens.sha256Hex(refreshToken),
                    account.subject(), account.authorities(), account.ownerId(), refreshTtl));
            return Either.right(new LoginOutput(access.token(), "Bearer", access.expiresInSeconds(), refreshToken));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }

    private static Notification unauthorized() {
        return Notification.create(new AuthenticationException(INVALID_CREDENTIALS));
    }
}
