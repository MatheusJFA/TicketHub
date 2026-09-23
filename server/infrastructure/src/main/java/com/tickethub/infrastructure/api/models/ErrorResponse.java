package com.tickethub.infrastructure.api.models;

import com.tickethub.domain.validation.Error;
import java.util.List;

public record ErrorResponse(List<Error> errors) {
    public static ErrorResponse from(String message) {
        return new ErrorResponse(List.of(new Error(message)));
    }
}
