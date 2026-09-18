package com.tickethub.infrastructure.api;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import com.tickethub.application.Either;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.validation.Notification;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

/**
 * Retry + circuit breaker around use case execution.
 *
 * <p>Only transient infrastructure failures are retried and counted by the
 * circuit breaker: a {@code Left} whose {@link Notification} carries a cause
 * that is not a {@link DomainException}. Domain validation errors
 * ({@code Left} without cause) pass through untouched, so 404/422 semantics
 * are preserved.
 */
public final class ResiliencePolicy {

    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private final boolean enabled;

    private ResiliencePolicy(final Retry retry, final CircuitBreaker circuitBreaker, final boolean enabled) {
        this.retry = retry;
        this.circuitBreaker = circuitBreaker;
        this.enabled = enabled;
    }

    public static ResiliencePolicy disabled() {
        return new ResiliencePolicy(null, null, false);
    }

    public static ResiliencePolicy of(final Retry retry, final CircuitBreaker circuitBreaker) {
        return new ResiliencePolicy(
                Objects.requireNonNull(retry, "'retry' should not be null"),
                Objects.requireNonNull(circuitBreaker, "'circuitBreaker' should not be null"),
                true);
    }

    public static boolean isTransientResult(final Object result) {
        if (result instanceof Either<?, ?> either && either.isLeft()) {
            return isTransientFailure(((Either<Notification, ?>) either).getLeft());
        }
        if (result instanceof Optional<?> optional && optional.orElse(null) instanceof Notification notification) {
            return isTransientFailure(notification);
        }
        return false;
    }

    private static boolean isTransientFailure(final Notification notification) {
        return notification.getCause() != null && !(notification.getCause() instanceof DomainException);
    }

    public <T> Supplier<T> decorate(final Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "'supplier' should not be null");
        if (!enabled) {
            return supplier;
        }
        return CircuitBreaker.decorateSupplier(circuitBreaker, Retry.decorateSupplier(retry, supplier));
    }
}
