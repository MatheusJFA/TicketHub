package com.tickethub.application.auth.refresh;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

import java.time.Duration;
import java.util.Objects;

import com.tickethub.application.Either;
import com.tickethub.domain.auth.AuthenticationException;
import com.tickethub.domain.auth.RefreshSession;
import com.tickethub.domain.auth.RefreshSessionGateway;
import com.tickethub.domain.auth.SecureTokens;
import com.tickethub.domain.auth.TokenIssuer;
import com.tickethub.domain.validation.Notification;

public class DefaultRefreshTokenUseCase extends RefreshTokenUseCase {

    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";

    private final TokenIssuer tokenIssuer;
    private final RefreshSessionGateway refreshSessions;
    private final Duration refreshTtl;

    public DefaultRefreshTokenUseCase(final TokenIssuer tokenIssuer,
            final RefreshSessionGateway refreshSessions, final Duration refreshTtl) {
        this.tokenIssuer = Objects.requireNonNull(tokenIssuer);
        this.refreshSessions = Objects.requireNonNull(refreshSessions);
        this.refreshTtl = isNull(refreshTtl) ? RefreshSession.DEFAULT_TTL : refreshTtl;
    }

    @Override
    public Either<Notification, RefreshTokenOutput> execute(final RefreshTokenCommand command) {
        try {
            final var presented = defaultString(command.refreshToken());
            final var session = refreshSessions.findByTokenHash(SecureTokens.sha256Hex(presented)).orElse(null);
            if (isNull(session) || !session.isActive()) {
                return Either.left(unauthorized());
            }
            if (session.isRotated()) {
                // Reuse of an already rotated token: possible theft, revoke the whole family.
                refreshSessions.findByFamilyId(session.getFamilyId()).forEach(found -> {
                    found.revoke();
                    refreshSessions.save(found);
                });
                return Either.left(unauthorized());
            }
            final var nextToken = SecureTokens.generateOpaqueToken();
            final var next = session.rotate(SecureTokens.sha256Hex(nextToken), refreshTtl);
            refreshSessions.save(session);
            refreshSessions.save(next);
            final var access = tokenIssuer.issueAccess(session.getSubject(), session.getAuthorities(),
                    session.getOwnerId());
            return Either.right(new RefreshTokenOutput(access.token(), "Bearer", access.expiresInSeconds(), nextToken));
        } catch (final RuntimeException exception) {
            return Either.left(Notification.create(exception));
        }
    }

    private static Notification unauthorized() {
        return Notification.create(new AuthenticationException(INVALID_REFRESH_TOKEN));
    }
}
