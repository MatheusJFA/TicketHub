package com.tickethub.domain.validation;

import static org.apache.commons.lang3.StringUtils.isBlank;

public record Error(String message) {
    public Error {
        if (isBlank(message)) {
            throw new IllegalArgumentException("'message' should not be null or blank");
        }
    }
}
