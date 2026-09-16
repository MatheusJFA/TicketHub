package com.tickethub.application.show.changename;

import com.tickethub.domain.core.show.Show;

import java.util.Objects;
import java.util.Optional;
import com.tickethub.application.Either;
import com.tickethub.domain.validation.Notification;

import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Error;

public final class DefaultChangeShowNameUseCase extends ChangeShowNameUseCase {
    private final ShowGateway showGateway;

    public DefaultChangeShowNameUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, ChangeShowNameOutput> execute(final ChangeShowNameCommand input) {
        try {
            final ShowID id = ShowID.from(input.id());
            final Optional<Show> found = showGateway.findById(id);
            if (!found.isPresent()) {
                return Either.left(notFound("Show", input.id()));
            }

            final Show entity = found.get();
            entity.changeName(input.name());
            final Notification notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final Show saved = showGateway.update(entity);
            final ChangeShowNameOutput output = ChangeShowNameOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

    
}
