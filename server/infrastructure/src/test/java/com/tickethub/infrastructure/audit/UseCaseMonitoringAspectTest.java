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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.application.UnitUseCase;
import com.tickethub.application.UseCase;
import com.tickethub.domain.authentication.AuthenticationException;
import com.tickethub.domain.validation.Error;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.api.ResiliencePolicy;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

@DisplayName("UseCaseMonitoringAspect")
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
    @DisplayName("Given success, when monitor, then returns value and records success")
    void givenSuccess_whenMonitor_thenReturnsValueAndRecordsSuccess() throws Throwable {
        final var target = useCase();

        final var result = aspect.monitor(joinPoint(target, invocation -> Either.right("ok-in")));

        assertEquals(Either.right("ok-in"), result,
                () -> "Monitor should return the use case value on success");
        assertEquals(1, entries.size(),
                () -> "Monitor should record exactly one audit entry on success");
        final var entry = entries.get(0);
        assertEquals(AuditOutcome.SUCCESS, entry.outcome(),
                () -> "Recorded entry should have SUCCESS outcome");
        assertEquals("corr-1", entry.correlationId(),
                () -> "Recorded entry should preserve the correlation id");
        assertEquals("alice", entry.actor(),
                () -> "Recorded entry should preserve the actor");
        assertNotNull(entry.occurredAt(),
                () -> "Recorded entry should have an occurredAt timestamp");
        assertTrue(entry.durationMs() >= 0,
                () -> "Recorded entry should have a non-negative duration");
    }

    @Test
    @DisplayName("Given not found, when monitor, then passes through and records not found")
    void givenNotFound_whenMonitor_thenPassesThroughAndRecordsNotFound() throws Throwable {
        final var target = useCase();
        final var failure = Either.<Notification, String>left(
                Notification.create(new Error("Spot not found: 1")));

        final var result = aspect.monitor(joinPoint(target, invocation -> failure));

        assertEquals(failure, result,
                () -> "Monitor should pass through the not-found failure unchanged");
        assertEquals(1, entries.size(),
                () -> "Monitor should record exactly one audit entry on not-found");
        assertEquals(AuditOutcome.NOT_FOUND, entries.get(0).outcome(),
                () -> "Recorded entry should have NOT_FOUND outcome");
        assertEquals("Spot not found: 1", entries.get(0).error(),
                () -> "Recorded entry should preserve the not-found error message");
    }

    @Test
    @DisplayName("Given validation error, when monitor, then records validation entry")
    void givenValidationError_whenMonitor_thenRecordsValidationEntry() throws Throwable {
        final var result = aspect.monitor(joinPoint(useCase(),
                invocation -> Either.left(Notification.create(new Error("bad")))));

        assertTrue(((Either<?, ?>) result).isLeft(),
                () -> "Monitor should pass through the validation failure");
        assertEquals(AuditOutcome.VALIDATION_ERROR, entries.get(0).outcome(),
                () -> "Recorded entry should have VALIDATION_ERROR outcome");
    }

    @Test
    @DisplayName("Given unit use case failure, when monitor, then records validation entry")
    void givenUnitUseCaseFailure_whenMonitor_thenRecordsValidationEntry() throws Throwable {
        final var target = new UnitUseCase<String>() {
            @Override
            public Optional<Notification> execute(final String input) {
                return Optional.of(Notification.create(new Error("bad")));
            }
        };

        final var result = aspect.monitor(joinPoint(target,
                invocation -> Optional.of(Notification.create(new Error("bad")))));

        assertTrue(result instanceof Optional<?>,
                () -> "Monitor should pass through the unit use case result");
        assertEquals(AuditOutcome.VALIDATION_ERROR, entries.get(0).outcome(),
                () -> "Recorded entry should have VALIDATION_ERROR outcome");
    }

    @Test
    @DisplayName("Given infra failure, when monitor, then records infra error entry")
    void givenInfraFailure_whenMonitor_thenRecordsInfraErrorEntry() throws Throwable {
        final var result = aspect.monitor(joinPoint(useCase(),
                invocation -> Either.left(Notification.create(new IllegalStateException("db down")))));

        assertTrue(((Either<?, ?>) result).isLeft(),
                () -> "Monitor should pass through the infrastructure failure");
        assertEquals(AuditOutcome.INFRASTRUCTURE_ERROR, entries.get(0).outcome(),
                () -> "Recorded entry should have INFRASTRUCTURE_ERROR outcome");
    }

    @Test
    @DisplayName("Given authentication failure, when monitor, then records unauthorized entry")
    void givenAuthenticationFailure_whenMonitor_thenRecordsUnauthorizedEntry() throws Throwable {
        final var result = aspect.monitor(joinPoint(useCase(),
                invocation -> Either.left(Notification.create(new AuthenticationException("Invalid credentials")))));

        assertTrue(((Either<?, ?>) result).isLeft(),
                () -> "Monitor should pass through the authentication failure");
        assertEquals(AuditOutcome.UNAUTHORIZED, entries.get(0).outcome(),
                () -> "Recorded entry should have UNAUTHORIZED outcome");
        assertEquals("Invalid credentials", entries.get(0).error(),
                () -> "Recorded entry should preserve the authentication error message");
    }

    @Test
    @DisplayName("Given unavailable, when monitor, then records unavailable entry")
    void givenUnavailable_whenMonitor_thenRecordsUnavailableEntry() throws Throwable {
        final var failure = Either.<Notification, String>left(Notification.create(
                new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "nope")));

        aspect.monitor(joinPoint(useCase(), invocation -> failure));

        assertEquals(AuditOutcome.UNAVAILABLE, entries.get(0).outcome(),
                () -> "Recorded entry should have UNAVAILABLE outcome");
    }

    @Test
    @DisplayName("Given throwing use case, when monitor, then rethrows and records infra error")
    void givenThrowingUseCase_whenMonitor_thenRethrowsAndRecordsInfraError() {
        final var joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(useCase());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"in"});
        try {
            when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        } catch (final Throwable t) {
            throw new AssertionError(t);
        }

        final var exception = assertThrows(IllegalStateException.class, () -> aspect.monitor(joinPoint),
                () -> "Monitor should rethrow the use case IllegalStateException");

        assertEquals("boom", exception.getMessage(),
                () -> "Rethrown exception should preserve the original message");
        assertEquals(AuditOutcome.INFRASTRUCTURE_ERROR, entries.get(0).outcome(),
                () -> "Recorded entry should have INFRASTRUCTURE_ERROR outcome");
    }

    @Test
    @DisplayName("Given sensitive input, when monitor, then records masked input")
    void givenSensitiveInput_whenMonitor_thenRecordsMaskedInput() throws Throwable {
        final var joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getTarget()).thenReturn(useCase());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"LoginCommand[password=admin-local]"});
        when(joinPoint.proceed()).thenReturn(Either.right("ok"));

        aspect.monitor(joinPoint);

        assertEquals(1, entries.size(),
                () -> "Monitor should record exactly one audit entry");
        assertEquals("[LoginCommand[password=***]]", entries.get(0).input(),
                () -> "Recorded entry should mask the sensitive password input");
    }

    @Test
    @DisplayName("Given failing trail, when monitor, then request still succeeds")
    void givenFailingTrail_whenMonitor_thenRequestStillSucceeds() throws Throwable {        final ObjectProvider<AuditTrail> trails = failingTrails();
        final var silent = new UseCaseMonitoringAspect(ResiliencePolicy.disabled(), trails);

        final var result = silent.monitor(joinPoint(useCase(), invocation -> Either.right("ok-in")));

        assertEquals(Either.right("ok-in"), result,
                () -> "Monitor should return the use case value even when the audit trail fails");
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
    @DisplayName("Given transient failures, when monitor, then retries and returns success")
    void givenTransientFailures_whenMonitor_thenRetriesAndReturnsSuccess() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        final var result = resilient.monitor(joinPoint(useCase(), invocation -> {
            final int call = calls.getAndIncrement();
            return call < 2 ? transientFailure() : Either.right("ok");
        }));

        assertEquals(Either.right("ok"), result,
                () -> "Monitor should return success after retrying transient failures");
        assertEquals(3, calls.get(),
                () -> "Monitor should have attempted the use case three times");
    }

    @Test
    @DisplayName("Given validation failure, when monitor, then does not retry")
    void givenValidationFailure_whenMonitor_thenDoesNotRetry() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        resilient.monitor(joinPoint(useCase(),
                invocation -> {
                    calls.getAndIncrement();
                    return Either.left(Notification.create(new Error("bad")));
                }));

        assertEquals(1, calls.get(),
                () -> "Monitor should not retry validation failures");
    }

    @Test
    @DisplayName("Given open circuit, when monitor, then returns 503 without calling use case")
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
                () -> resilient.monitor(joinPoint),
                () -> "Monitor should throw SERVICE_UNAVAILABLE when the circuit is open");

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode(),
                () -> "Exception status should be SERVICE_UNAVAILABLE");
        assertEquals(0, calls.get(),
                () -> "Monitor should not call the use case when the circuit is open");
    }

    @Test
    @DisplayName("Given exhausted retries, when monitor, then returns last failure")
    void givenExhaustedRetries_whenMonitor_thenReturnsLastFailure() throws Throwable {
        final ObjectProvider<AuditTrail> trails = trails();
        final var resilient = new UseCaseMonitoringAspect(policy(3, closedBreaker()), trails);

        final var result = resilient.monitor(joinPoint(useCase(), invocation -> {
            calls.getAndIncrement();
            return transientFailure();
        }));

        assertTrue(((Either<?, ?>) result).isLeft(),
                () -> "Monitor should return the last failure after retries are exhausted");
        assertEquals(3, calls.get(),
                () -> "Monitor should have attempted the use case three times");
    }
}
