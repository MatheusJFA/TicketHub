package com.tickethub.application;

import com.tickethub.domain.validation.Notification;
import com.tickethub.domain.validation.Error;

public abstract class NullaryUseCase<OUT> {

    public abstract OUT execute();

    protected final Notification notFound(final String resource, final String id) {
        return Notification.create(new Error(resource + " not found: " + id));
    }
}
