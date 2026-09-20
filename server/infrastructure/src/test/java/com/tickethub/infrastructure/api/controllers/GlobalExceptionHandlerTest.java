package com.tickethub.infrastructure.api.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.tickethub.domain.exception.DomainException;
import com.tickethub.infrastructure.exception.EventPublishException;
import com.tickethub.infrastructure.exception.InfrastructureException;

@DisplayName("Global exception handler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Given infrastructure failure, when handle, then returns service unavailable")
    void givenInfrastructureFailure_whenHandle_thenReturnsServiceUnavailable() {
        final var response = handler.infrastructure(
                new EventPublishException("Failed to publish spot generation event",
                        new IllegalStateException("mapper down")));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Failed to publish spot generation event",
                response.getBody().errors().get(0).message());
    }

    @Test
    @DisplayName("Given infrastructure base type, when handle, then returns service unavailable")
    void givenInfrastructureBaseType_whenHandle_thenReturnsServiceUnavailable() {
        final var response = handler.infrastructure(new InfrastructureException("db down"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    @DisplayName("Given domain failure, when handle, then returns unprocessable entity")
    void givenDomainFailure_whenHandle_thenReturnsUnprocessableEntity() {
        final var response = handler.domain(new DomainException("Invalid money"));

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("Invalid money", response.getBody().errors().get(0).message());
    }
}
