package com.tickethub.domain.validation;

public record Error(String message) {
    public Error {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("'message' should not be null or blank");
        }
    }
}
