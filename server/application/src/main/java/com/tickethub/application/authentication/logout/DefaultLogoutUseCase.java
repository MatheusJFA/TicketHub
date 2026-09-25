package com.tickethub.application.authentication.logout;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.tickethub.domain.authentication.AccessTokenInspector;
import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import com.tickethub.domain.authentication.SecureTokens;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public class DefaultLogoutUseCase extends LogoutUseCase {

    private final RefreshSessionGateway refreshSessions;
    private final RevokedAccessTokenGateway revokedAccessTokens;
    private final AccessTokenInspector accessTokenInspector;

    public DefaultLogoutUseCase(
            final RefreshSessionGateway refreshSessions,
            final RevokedAccessTokenGateway revokedAccessTokens,
            final AccessTokenInspector accessTokenInspector) {
        this.refreshSessions = requireNonNull(refreshSessions);
        this.revokedAccessTokens = requireNonNull(revokedAccessTokens);
        this.accessTokenInspector = requireNonNull(accessTokenInspector);
    }

    @Override
    public Optional<Notification> execute(final LogoutCommand command) {
        try {
            final var presented = defaultString(command.refreshToken());
            refreshSessions.findByTokenHash(SecureTokens.sha256Hex(presented)).ifPresent(session -> {
                session.revoke();
                refreshSessions.save(session);
            });
            if (isNotBlank(command.accessToken())) {
                accessTokenInspector
                        .inspect(command.accessToken())
                        .ifPresent(identity -> revokedAccessTokens.revoke(identity.tokenId(), identity.expiresAt()));
            }
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
