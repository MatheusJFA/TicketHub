package com.tickethub.application;

import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

public abstract class UseCase<IN, OUT> {

    public abstract OUT execute(IN input);

    protected final Notification notFound(final String resource, final String id) {
        return Notification.create(new Error(resource + " not found: " + id));
    }
}
