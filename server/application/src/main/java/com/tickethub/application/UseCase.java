package com.tickethub.application;

import com.tickethub.domain.exception.ResourceNotFoundException;
import com.tickethub.domain.validation.Notification;
import java.util.Optional;

public abstract class UseCase<IN, OUT> {

    public abstract OUT execute(IN input);

    protected final Notification notFound(final String resource, final String id) {
        return Notification.create(new ResourceNotFoundException(resource, id));
    }

    protected final <T> Either<Notification, T> findOrNotFound(
            final Optional<T> found, final String resource, final String id) {
        return found.<Either<Notification, T>>map(Either::right).orElseGet(() -> Either.left(notFound(resource, id)));
    }
}
