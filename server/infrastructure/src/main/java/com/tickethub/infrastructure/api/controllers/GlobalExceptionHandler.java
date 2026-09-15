package com.tickethub.infrastructure.api.controllers;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.infrastructure.api.ApiValidationException;
import com.tickethub.infrastructure.api.models.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiValidationException.class)
    ResponseEntity<ErrorResponse> validation(ApiValidationException exception) {
        final var errors = exception.notification().getErrors();
        // Existing use cases represent missing entities as notifications.
        final boolean missing = errors.stream().anyMatch(error ->
                error.message().matches("^(Customer|Partner|Show|Section|Spot) not found: .*$"));
        return ResponseEntity.status(missing ? 404 : 422).body(new ErrorResponse(errors));
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ErrorResponse> domain(DomainException exception) {
        return ResponseEntity.unprocessableEntity().body(ErrorResponse.from(exception.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> status(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .body(ErrorResponse.from(exception.getReason()));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        LOG.error("Unexpected API failure", exception);
        return ResponseEntity.internalServerError().body(ErrorResponse.from("Internal server error"));
    }
}
