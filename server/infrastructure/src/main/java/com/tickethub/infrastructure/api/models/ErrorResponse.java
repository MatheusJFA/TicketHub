package com.tickethub.infrastructure.api.models;

import java.util.List;
import com.tickethub.domain.validation.Error;

public record ErrorResponse(List<Error> errors) {
    public static ErrorResponse from(String message) {
        return new ErrorResponse(List.of(new Error(message)));
    }
}
