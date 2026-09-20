package com.tickethub.application.authentication.logout;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.domain.authentication.RefreshSessionGateway;
import com.tickethub.domain.authentication.SecureTokens;
import com.tickethub.domain.validation.Notification;

public class DefaultLogoutUseCase extends LogoutUseCase {

    private final RefreshSessionGateway refreshSessions;

    public DefaultLogoutUseCase(final RefreshSessionGateway refreshSessions) {
        this.refreshSessions = Objects.requireNonNull(refreshSessions);
    }

    @Override
    public Optional<Notification> execute(final LogoutCommand command) {
        try {
            final var presented = defaultString(command.refreshToken());
            refreshSessions.findByTokenHash(SecureTokens.sha256Hex(presented)).ifPresent(session -> {
                session.revoke();
                refreshSessions.save(session);
            });
            return Optional.empty();
        } catch (final RuntimeException exception) {
            return Optional.of(Notification.create(exception));
        }
    }
}
