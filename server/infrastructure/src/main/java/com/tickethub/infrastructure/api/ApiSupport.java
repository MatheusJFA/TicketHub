package com.tickethub.infrastructure.api;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.tickethub.application.Either;
import com.tickethub.application.UseCase;
import com.tickethub.domain.exception.DomainException;
import com.tickethub.domain.pagination.SearchQuery;
import com.tickethub.domain.validation.Notification;
import com.tickethub.infrastructure.audit.AuditEntry;
import com.tickethub.infrastructure.audit.AuditOutcome;
import com.tickethub.infrastructure.audit.AuditTrail;
import com.tickethub.infrastructure.web.CorrelationIdFilter;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public final class ApiSupport {
    private ApiSupport() {}

    private static final Logger LOG = LoggerFactory.getLogger(ApiSupport.class);

    private static volatile ResiliencePolicy resilience = ResiliencePolicy.disabled();
    private static volatile AuditTrail auditTrail = AuditTrail.noop();

    public static void configureResilience(final ResiliencePolicy policy) {
        resilience = Objects.requireNonNull(policy, "'policy' should not be null");
    }

    public static void configureAuditTrail(final AuditTrail trail) {
        auditTrail = Objects.requireNonNull(trail, "'trail' should not be null");
    }

    public static <I, O> O execute(UseCase<I, Either<Notification, O>> useCase, I input) {
        final String action = useCase.getClass().getSimpleName();
        final String inputSummary = String.valueOf(input);
        final long startedAt = System.nanoTime();
        final Either<Notification, O> result;
        try {
            final Supplier<Either<Notification, O>> call = () -> useCase.execute(input);
            result = resilience.decorate(call).get();
        } catch (final CallNotPermittedException e) {
            final var status = new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Service temporarily unavailable", e);
            audit(action, inputSummary, startedAt, AuditOutcome.UNAVAILABLE, status.getReason());
            throw status;
        }
        if (result.isRight()) {
            audit(action, inputSummary, startedAt, AuditOutcome.SUCCESS, null);
            return result.getRight();
        }
        final Notification notification = result.getLeft();
        audit(action, inputSummary, startedAt, outcomeOf(notification), firstMessage(notification));
        if (notification.getCause() instanceof ResponseStatusException status) {
            throw status;
        }

        if (notification.getCause() != null && !(notification.getCause() instanceof DomainException)) {
            throw new IllegalStateException("Use case failed", notification.getCause());
        }
        throw new ApiValidationException(notification);
    }

    private static void audit(final String action, final String input, final long startedAt,
            final AuditOutcome outcome, final String error) {
        final long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        final var entry = new AuditEntry(Instant.now(),
                MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY),
                MDC.get(CorrelationIdFilter.ACTOR_KEY),
                action, input, outcome, error, durationMs);
        LOG.info("audit action={} outcome={} durationMs={} correlationId={} actor={}{}",
                action, outcome, durationMs, entry.correlationId(), entry.actor(),
                error == null ? "" : " error=" + error);
        try {
            auditTrail.record(entry);
        } catch (final RuntimeException e) {
            LOG.warn("audit trail write failed for action={}", action, e);
        }
    }

    private static AuditOutcome outcomeOf(final Notification notification) {
        if (notification.getCause() instanceof ResponseStatusException status
                && status.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
            return AuditOutcome.UNAVAILABLE;
        }
        if (notification.getCause() != null) {
            return AuditOutcome.INFRA_ERROR;
        }
        // Same convention as GlobalExceptionHandler: missing entities are notifications.
        final boolean missing = notification.getErrors().stream()
                .anyMatch(error -> error.message() != null && error.message().matches("^.+ not found: .*$"));
        return missing ? AuditOutcome.NOT_FOUND : AuditOutcome.VALIDATION_ERROR;
    }

    private static String firstMessage(final Notification notification) {
        final var first = notification.firstError();
        return first == null ? null : first.message();
    }

    public static SearchQuery query(String search, int page, int perPage, String sort, String direction) {        if (page < 0 || perPage < 1 || perPage > 100 || sort == null || sort.isBlank()
                || direction == null
                || !(direction.equalsIgnoreCase("asc") || direction.equalsIgnoreCase("desc"))) {
            throw new DomainException("Invalid pagination parameters: page >= 0, perPage between 1 and 100, sort non-blank, dir asc or desc");
        }
        return new SearchQuery(page, perPage, search, sort, direction.toLowerCase(Locale.ROOT));
    }
}
