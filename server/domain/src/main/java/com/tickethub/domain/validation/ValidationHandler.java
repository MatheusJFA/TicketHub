package com.tickethub.domain.validation;

import java.util.List;

public interface ValidationHandler {

    ValidationHandler append(Error error);

    ValidationHandler append(ValidationHandler handler);

    <T> T validate(Validation<T> validation);

    List<Error> getErrors();

    default boolean hasError() {
        return getErrors() != null && !getErrors().isEmpty();
    }

    default Error firstError() {
        return hasError() ? getErrors().get(0) : null;
    }

    default Error lastError() {
        final var lastElement = getErrors().size() - 1;
        return hasError() ? getErrors().get(lastElement) : null;
    }

    interface Validation<T> {
        T validate();
    }
}
