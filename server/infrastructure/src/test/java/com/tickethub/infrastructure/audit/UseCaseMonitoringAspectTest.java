package com.tickethub.infrastructure.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.application.UnitUseCase;
import com.tickethub.application.UseCase;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.api.ResiliencePolicy;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

class UseCaseMonitoringAspectTest {

    private final List<AuditEntry> entries = new ArrayList<>();
    private final AtomicInteger calls = new AtomicInteger();

    private UseCaseMonitoringAspect aspect;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        entries.clear();
        calls.set(0);
        MDC.put(CorrelationIdFilter.CORRELATION_ID_KEY, "corr-1");
        MDC.put(CorrelationIdFilter.ACTOR_KEY, "alice");
        final ObjectProvider<AuditTrail> trails = trails();
        aspect = new UseCaseMonitoringAspect(ResiliencePolicy.disabled(), trails);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<AuditTrail> trails() {
        final ObjectProvider<AuditTrail> trails = mock();
        doReturn((AuditTrail) entries::add).when(trails).getIfAvailable(any());
        return trails;
    }

    @SuppressWarnings("unchecked")
    private ObjectProvider<AuditTrail> failingTrails() {
        final ObjectProvider<AuditTrail> trails = mock();
        final AuditTrail failing = entry -> {
            throw new IllegalStateException("trail down");
        };
        doReturn(failing).when(trails).getIfAvailable(any());
        return trails;
    }

    private ProceedingJoinPoint joinPoint(final Object target, final Answer<?> answer) throws Throwable {
        final var joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"in"});
        when(joinPoint.proceed()).thenAnswer(answer);
        return joinPoint;
    }

    private ProceedingJoinPoint unchecked(final Object target, final Answer<?> answer) {
        try {
            return joinPoint(target, answer);
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }
    }

    private UseCase<String, Either<Notification, String>> useCase() {
        return new UseCase<>() {
            @Override
            public Either<Notification, String> execute(final String input) {
                return Either.right("ok-" + input);
            }
        };
    }

    @Test
    void givenSuccess_whenMonitor_thenReturnsValueAndRecordsSuccess() throws Throwable {
        final var target = useCase();

        final var result = aspect.monitor(joinPoint(target, invocation -> Either.right("ok-in")));

        assertEquals(Either.right("ok-in"), result);
        assertEquals(1, entries.size());
        final var entry = entries.get(0);
        assertEquals(AuditOutcome.SUCCESS, entry.outcome());
        assertEquals("corr-1", entry.correlationId());
        assertEquals("alice", entry.actor());
        assertNotNull(entry.occurredAt());
        assertTrue(entry.durationMs() >= 0);
    }

    @Test
    void givenNotFound_whenMonitor_thenPassesThroughAndRecordsNotFound() throws Throwable {
        final var target = useCase();
        final var failure = Either.<Notification, String>left(
                Notification.create(new Error("Spot not found: 1")));

        final var result = aspect.monitor(joinPoint(target, invocation -> failure));

        assertEquals(failure, result);
        assertEquals(1, entries.size());
        assertEquals(AuditOutcome.NOT_FOUND, entries.get(0).outcome());
        assertEquals("Spot not found: 1", entries.get(0).error());
    }

    @Test
    void givenValidationError_whenMonitor_thenRecordsValidationEntry() throws Throwable {
        final var result = aspect.monitor(joinPoint(useCase(),
                invocation -> Either.left(Notification.create(new Error("bad")))));

        assertTrue(((Either<?, ?>) result).isLeft());
        assertEquals(AuditOutcome.VALIDATION_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenUnitUseCaseFailure_whenMonitor_thenRecordsValidationEntry() throws Throwable {
        final var target = new UnitUseCase<String>() {
            @Override
            public Optional<Notification> execute(final String input) {
                return Optional.of(Notification.create(new Error("bad")));
            }
        };

        final var result = aspect.monitor(joinPoint(target,
                invocation -> Optional.of(Notification.create(new Error("bad")))));

        assertTrue(result instanceof Optional<?>);
        assertEquals(AuditOutcome.VALIDATION_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenInfraFailure_whenMonitor_thenRecordsInfraErrorEntry() throws Throwable {
        final var result = aspect.monitor(joinPoint(useCase(),
                invocation -> Either.left(Notification.create(new IllegalStateException("db down")))));

        assertTrue(((Either<?, ?>) result).isLeft());
        assertEquals(AuditOutcome.INFRA_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenUnavailable_whenMonitor_thenRecordsUnavailableEntry() throws Throwable {
        final var failure = Either.<Notification, String>left(Notification.create(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "nope")));

        aspect.monitor(joinPoint(useCase(), invocation -> failure));

        assertEquals(AuditOutcome.UNAVAILABLE, entries.get(0).outcome());
    }

    @Test
    void givenThrowingUseCase_whenMonitor_thenRethrowsAndRecordsInfraError() {
        final var joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(useCase());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"in"});
        try {
            when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }

        assertThrows(IllegalStateException.class, () -> aspect.monitor(joinPoint));
        assertEquals(AuditOutcome.INFRA_ERROR, entries.get(0).outcome());
    }

    @Test
    void givenFailingTrail_whenMonitor_thenRequestStillSucceeds() throws Throwable {
        final ObjectProvider<AuditTrail> trails = failingTrails();
        final var silent = new UseCaseMonitoringAspect(ResiliencePolicy.disabled(), trails);

        final var result = silent.monitor(joinPoint(useCase(), invocation -> Either.right("ok-in")));

        assertEquals(Either.right("ok-in"), result);
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
    void givenTransientFailures_whenMonitor_thenRetriesAndReturnsSuccess() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        final var result = resilient.monitor(joinPoint(useCase(), invocation -> {
            final int call = calls.getAndIncrement();
            return call < 2 ? transientFailure() : Either.right("ok");
        }));

        assertEquals(Either.right("ok"), result);
        assertEquals(3, calls.get());
    }

    @Test
    void givenValidationFailure_whenMonitor_thenDoesNotRetry() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        resilient.monitor(joinPoint(useCase(),
                invocation -> {
                    calls.getAndIncrement();
                    return Either.left(Notification.create(new Error("bad")));
                }));

        assertEquals(1, calls.get());
    }

    @Test
    void givenOpenCircuit_whenMonitor_thenReturns503WithoutCallingUseCase() {
        final ObjectProvider<AuditTrail> trails = trails();
        final var circuitBreaker = closedBreaker();
        circuitBreaker.transitionToOpenState();
        final var resilient = new UseCaseMonitoringAspect(policy(3, circuitBreaker), trails);
        final var joinPoint = unchecked(useCase(), invocation -> {
            calls.getAndIncrement();
            return Either.right("ok");
        });

        final var exception = assertThrows(ResponseStatusException.class,
                () -> resilient.monitor(joinPoint));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        assertEquals(0, calls.get());
    }

    @Test
    void givenExhaustedRetries_whenMonitor_thenReturnsLastFailure() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        final var result = resilient.monitor(joinPoint(useCase(), invocation -> {
            calls.getAndIncrement();
            return transientFailure();
        }));

        assertTrue(((Either<?, ?>) result).isLeft());
        assertEquals(3, calls.get());
    }
}
