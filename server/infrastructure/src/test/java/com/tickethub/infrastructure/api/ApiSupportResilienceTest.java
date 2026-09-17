package com.tickethub.infrastructure.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

class ApiSupportResilienceTest {

    private final AtomicInteger calls = new AtomicInteger();

    @AfterEach
    void resetResilience() {
        ApiSupport.configureResilience(ResiliencePolicy.disabled());
    }

    private UseCase<String, Either<Notification, String>> scripted(final Either<Notification, String>... results) {
        return new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return results[calls.getAndIncrement()];
            }
        };
    }

    private ResiliencePolicy policy(final int maxAttempts, final CircuitBreaker circuitBreaker) {
        final Retry retry = Retry.of("test", RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(1))
                .retryOnResult(ResiliencePolicy::isTransientResult)
                .build());
        return ResiliencePolicy.of(retry, circuitBreaker);
    }

    private CircuitBreaker closedBreaker() {
        return CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .minimumNumberOfCalls(10)
                .slidingWindowSize(10)
                .recordResult(ResiliencePolicy::isTransientResult)
                .build());
    }

    private static Either<Notification, String> transientFailure() {
        return Either.left(Notification.create(new IllegalStateException("boom")));
    }

    @Test
    void givenTransientFailures_whenExecute_thenRetriesAndReturnsSuccess() {
        ApiSupport.configureResilience(policy(3, closedBreaker()));
        final var useCase = scripted(transientFailure(), transientFailure(), Either.right("ok"));

        final var output = ApiSupport.execute(useCase, "in");

        assertEquals("ok", output);
        assertEquals(3, calls.get());
    }

    @Test
    void givenExhaustedRetries_whenExecute_thenPropagatesLastFailure() {
        ApiSupport.configureResilience(policy(3, closedBreaker()));
        final var useCase = scripted(transientFailure(), transientFailure(), transientFailure());

        assertThrows(IllegalStateException.class, () -> ApiSupport.execute(useCase, "in"));
        assertEquals(3, calls.get());
    }

    @Test
    void givenValidationFailure_whenExecute_thenDoesNotRetry() {
        ApiSupport.configureResilience(policy(3, closedBreaker()));
        final var useCase = scripted(Either.left(Notification.create(new Error("bad"))));

        assertThrows(ApiValidationException.class, () -> ApiSupport.execute(useCase, "in"));
        assertEquals(1, calls.get());
    }

    @Test
    void givenOpenCircuit_whenExecute_thenReturns503WithoutCallingUseCase() {
        final var circuitBreaker = closedBreaker();
        circuitBreaker.transitionToOpenState();
        ApiSupport.configureResilience(policy(3, circuitBreaker));
        final var useCase = scripted(Either.right("ok"));

        final var exception = assertThrows(ResponseStatusException.class,
                () -> ApiSupport.execute(useCase, "in"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals(0, calls.get());
    }
}
