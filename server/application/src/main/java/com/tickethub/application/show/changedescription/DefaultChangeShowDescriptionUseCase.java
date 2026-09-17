package com.tickethub.application.show.changedescription;

import java.util.Objects;
import java.util.Optional;

import com.tickethub.application.Either;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;
import com.tickethub.domain.validation.Notification;

public final class DefaultChangeShowDescriptionUseCase extends ChangeShowDescriptionUseCase {
    private final ShowGateway showGateway;

    public DefaultChangeShowDescriptionUseCase(final ShowGateway showGateway) {
        this.showGateway = Objects.requireNonNull(showGateway);
    }

    @Override
    public Either<Notification, ChangeShowDescriptionOutput> execute(final ChangeShowDescriptionCommand input) {
        try {
            final ShowID id = ShowID.from(input.id());
            final Optional<Show> found = showGateway.findById(id);
            if (!found.isPresent()) {
                return Either.left(notFound(Show.class.getSimpleName(), id.getValue()));
            }
            final Show entity = found.get();
            entity.changeDescription(input.description());
            final Notification notification = Notification.create();
            entity.validate(notification);
            if (notification.hasError()) {
                return Either.left(notification);
            }
            final Show saved = showGateway.update(entity);
            final ChangeShowDescriptionOutput output = ChangeShowDescriptionOutput.from(saved);
            return Either.right(output);
        } catch (final RuntimeException exception) {
            final Notification notification = Notification.create(exception);
            return Either.left(notification);
        }
    }

}
