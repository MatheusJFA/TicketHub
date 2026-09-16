package com.tickethub.application.show.delete;

import java.util.Objects;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;


public final class DefaultDeleteShowUseCase extends DeleteShowUseCase {
    private final ShowGateway showGateway;

    public DefaultDeleteShowUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, DeleteShowOutput> execute(final String input) {
        try {
            final ShowID id = ShowID.from(input);
            showGateway.deleteById(id);

            final DeleteShowOutput output = DeleteShowOutput.from(input);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }
}
