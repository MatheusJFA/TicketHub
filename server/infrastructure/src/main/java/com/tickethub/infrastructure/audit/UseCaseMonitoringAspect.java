package com.tickethub.infrastructure.audit;

import static java.util.Objects.nonNull;

import com.tickethub.application.Either;
import com.tickethub.domain.authentication.AuthenticationException;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.api.ResiliencePolicy;
import com.tickethub.infrastructure.exception.InfrastructureException;
import com.tickethub.infrastructure.web.CorrelationIdFilter;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * Cross-cutting execution of application use cases: retry + circuit breaking
 * for transient failures and a best-effort audit entry per execution.
 */
@Aspect
@Component
public class UseCaseMonitoringAspect {

    private static final Logger LOG = LoggerFactory.getLogger(UseCaseMonitoringAspect.class);

    private final ResiliencePolicy resilience;
    private final ObjectProvider<AuditTrail> trails;
    private final UseCaseMetrics metrics;

    public UseCaseMonitoringAspect(
            final ResiliencePolicy resilience, final ObjectProvider<AuditTrail> trails, final UseCaseMetrics metrics) {
        this.resilience = resilience;
        this.trails = trails;
        this.metrics = metrics;
    }

    @Around("execution(* com.tickethub.application..*UseCase.execute(..))")
    public Object monitor(final ProceedingJoinPoint joinPoint) throws Throwable {
        final String action = ClassUtils.getUserClass(joinPoint.getTarget()).getSimpleName();
        final var inputSummary = AuditSanitizer.sanitize(Arrays.toString(joinPoint.getArgs()));
        final long startedAt = System.nanoTime();
        final Object result;
        try {
            result = resilience
                    .decorate(() -> {
                        try {
                            return joinPoint.proceed();
                        } catch (final RuntimeException | Error e) {
                            throw e;
                        } catch (final Throwable t) {
                            throw new InfrastructureException("Use case failed", t);
                        }
                    })
                    .get();
        } catch (final CallNotPermittedException e) {
            final var status =
                    new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable", e);
            audit(action, inputSummary, startedAt, AuditOutcome.UNAVAILABLE, Optional.ofNullable(status.getReason()));
            throw status;
        } catch (final Throwable t) {
            audit(
                    action,
                    inputSummary,
                    startedAt,
                    AuditOutcome.INFRASTRUCTURE_ERROR,
                    Optional.ofNullable(t.getMessage()));
            throw t;
        }
        failureOf(result)
                .ifPresentOrElse(
                        notification -> audit(
                                action, inputSummary, startedAt, outcomeOf(notification), firstMessage(notification)),
                        () -> audit(action, inputSummary, startedAt, AuditOutcome.SUCCESS, Optional.empty()));
        return result;
    }

    private void audit(
            final String action,
            final Optional<String> input,
            final long startedAt,
            final AuditOutcome outcome,
            final Optional<String> error) {
        final long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        metrics.record(action, outcome, System.nanoTime() - startedAt);
        final var entry = new AuditEntry(
                Instant.now(),
                MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY),
                MDC.get(CorrelationIdFilter.ACTOR_KEY),
                action,
                input.orElse(""),
                outcome,
                error.orElse(null),
                durationMs);
        LOG.info(
                "audit action={} outcome={} durationMs={} correlationId={} actor={}{}",
                action,
                outcome,
                durationMs,
                entry.correlationId(),
                entry.actor(),
                error.map(message -> " error=" + message).orElse(""));
        try {
            trails.getIfAvailable(AuditTrail::noop).record(entry);
        } catch (final RuntimeException e) {
            LOG.warn("audit trail write failed for action={}", action, e);
        }
    }

    private static Optional<Notification> failureOf(final Object result) {
        if (result instanceof Either<?, ?> either && either.isLeft()) {
            return Optional.of(((Either<Notification, ?>) either).getLeft());
        }
        if (result instanceof Optional<?> optional) {
            return optional.filter(Notification.class::isInstance).map(Notification.class::cast);
        }
        return Optional.empty();
    }

    private static AuditOutcome outcomeOf(final Notification notification) {
        if (notification.getCause() instanceof ResponseStatusException status
                && status.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            return AuditOutcome.UNAVAILABLE;
        }
        if (notification.getCause() instanceof AuthenticationException) {
            return AuditOutcome.UNAUTHORIZED;
        }
        if (nonNull(notification.getCause())) {
            return AuditOutcome.INFRASTRUCTURE_ERROR;
        }
        // Mesma convencao do GlobalExceptionHandler: entidades ausentes sao notificacoes.
        // Filtra: mensagens de entidade nao encontrada no formato "X not found: id".
        final boolean missing = notification.getErrors().stream()
                .anyMatch(error -> nonNull(error.message()) && error.message().matches("^.+ not found: .*$"));
        return missing ? AuditOutcome.NOT_FOUND : AuditOutcome.VALIDATION_ERROR;
    }

    private static Optional<String> firstMessage(final Notification notification) {
        return Optional.ofNullable(notification.firstError()).map(first -> first.message());
    }
}
