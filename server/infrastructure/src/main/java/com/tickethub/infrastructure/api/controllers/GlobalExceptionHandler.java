package com.tickethub.infrastructure.api.controllers;

import static java.util.Objects.nonNull;

import com.tickethub.domain.authentication.AuthenticationException;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.infrastructure.api.ApiValidationException;
import com.tickethub.infrastructure.api.models.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiValidationException.class)
    ResponseEntity<ErrorResponse> validation(ApiValidationException exception) {
        final var errors = exception.notification().getErrors();
        // Existing use cases represent missing entities as notifications ("X not found: id").
        final boolean missing = errors.stream().anyMatch(error ->
                nonNull(error.message()) && error.message().matches("^.+ not found: .*$"));
        return ResponseEntity.status(missing ? 404 : 422).body(new ErrorResponse(errors));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class, NoResourceFoundException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception exception) {
        final String message = exception instanceof MethodArgumentNotValidException validation
                ? validation.getBindingResult().getFieldErrors().stream()
                        .map(field -> "'" + field.getField() + "' " + field.getDefaultMessage())
                        .findFirst().orElse("Invalid request")
                : "Invalid request";
        return ResponseEntity.badRequest().body(ErrorResponse.from(message));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> authentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse.from(exception.getMessage()));
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

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> forbidden(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse.from("Access denied"));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> unexpected(Exception exception) {
        LOG.error("Unexpected API failure", exception);
        return ResponseEntity.internalServerError().body(ErrorResponse.from("Internal server error"));
    }
}
