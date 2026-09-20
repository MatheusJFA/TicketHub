package com.tickethub.domain.validation;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;
import java.util.Optional;

public interface ValidationHandler {

    ValidationHandler append(Error error);

    ValidationHandler append(ValidationHandler handler);

    <T> T validate(Validation<T> validation);

    List<Error> getErrors();

    default boolean hasError() {
        return !isEmpty(getErrors());
    }

    default Error firstError() {
        return Optional.ofNullable(getErrors()).filter(errors -> !errors.isEmpty())
                .map(errors -> errors.get(0)).orElse(null);
    }

    default Error lastError() {
        return Optional.ofNullable(getErrors()).filter(errors -> !errors.isEmpty())
                .map(errors -> errors.get(errors.size() - 1)).orElse(null);
    }

    interface Validation<T> {
        T validate();
    }
}
