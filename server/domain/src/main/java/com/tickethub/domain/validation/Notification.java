package com.tickethub.domain.validation;

import static java.util.Objects.requireNonNull;
import com.tickethub.domain.exception.DomainException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Notification implements ValidationHandler {

    private final List<Error> errors;
    private Throwable cause;

    private Notification(final List<Error> errors) {
        this.errors = errors;
    }

    public static Notification create() {
        return new Notification(new ArrayList<>());
    }

    public static Notification create(final Error error) {
        return create().append(error);
    }

    public static Notification create(final Throwable throwable) {
        final var notification = create(new Error(Objects.requireNonNullElse(throwable.getMessage(), throwable.getClass().getSimpleName())));
        notification.cause = throwable;
        return notification;
    }

    public Throwable getCause() {
        return cause;
    }

    @Override
    public Notification append(final Error error) {
        requireNonNull(error, "'error' should not be null");
        errors.add(error);
        return this;
    }

    @Override
    public Notification append(final ValidationHandler handler) {
        requireNonNull(handler, "'handler' should not be null");
        errors.addAll(handler.getErrors());
        return this;
    }

    @Override
    public <T> T validate(final Validation<T> validation) {
        try {
            return validation.validate();
        } catch (final DomainException e) {
            errors.add(new Error(Objects.requireNonNullElse(e.getMessage(), DomainException.class.getSimpleName())));
        } catch (final RuntimeException e) {
            errors.add(new Error(Objects.requireNonNullElse(e.getMessage(), e.getClass().getSimpleName())));
        }
        return null;
    }

    @Override
    public List<Error> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
