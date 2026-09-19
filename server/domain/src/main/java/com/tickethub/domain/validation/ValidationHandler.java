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
        if (!hasError()) {
            return null;
        }
        
        return getErrors().get(getErrors().size() - 1);
    }

    interface Validation<T> {
        T validate();
    }
}
