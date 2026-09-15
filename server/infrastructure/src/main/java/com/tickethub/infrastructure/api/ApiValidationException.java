package com.tickethub.infrastructure.api;

import com.tickethub.domain.validation.Notification;

public final class ApiValidationException extends RuntimeException {
    private final Notification notification;

    public ApiValidationException(Notification notification) {
        super("Request failed validation");
        this.notification = notification;
    }

    public Notification notification() {
        return notification;
    }
}
