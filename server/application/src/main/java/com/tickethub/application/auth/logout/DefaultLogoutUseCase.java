package com.tickethub.application.auth.logout;

import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.tickethub.domain.auth.RefreshSessionGateway;
import com.tickethub.domain.auth.SecureTokens;
import com.tickethub.domain.validation.Notification;

public class DefaultLogoutUseCase extends LogoutUseCase {

    private final RefreshSessionGateway refreshSessions;

    public DefaultLogoutUseCase(final RefreshSessionGateway refreshSessions) {
        this.refreshSessions = Objects.requireNonNull(refreshSessions);
    }

    @Override
    public Optional<Notification> execute(final LogoutCommand command) {
        try {
            final var presented = StringUtils.defaultString(command.refreshToken());
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
