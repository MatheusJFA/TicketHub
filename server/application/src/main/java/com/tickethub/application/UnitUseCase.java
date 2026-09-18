package com.tickethub.application;

import java.util.Optional;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public abstract class UnitUseCase<IN> {

    /**
     * Executes the use case. An empty result means success; a present
     * {@link Notification} carries validation or infrastructure errors,
     * following the same conventions as {@link UseCase}.
     */
    public abstract Optional<Notification> execute(IN input);

    protected final Notification notFound(final String resource, final String id) {
        return Notification.create(new Error(resource + " not found: " + id));
    }
}
