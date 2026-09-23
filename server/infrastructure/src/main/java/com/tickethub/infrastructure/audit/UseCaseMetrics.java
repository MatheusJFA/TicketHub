package com.tickethub.infrastructure.audit;

import static java.util.Objects.requireNonNull;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Component;

/**
 * Business-observable use case executions, recorded by
 * {@code UseCaseMonitoringAspect} for every use case: counts by action and
 * outcome plus duration. Payment conversion, for example, is the ratio of
 * {@code DefaultConfirmPaymentUseCase/SUCCESS} over
 * {@code DefaultCreateOrderUseCase/SUCCESS}.
 */
@Component
public class UseCaseMetrics {

    private final MeterRegistry registry;

    public UseCaseMetrics(final MeterRegistry registry) {
        this.registry = requireNonNull(registry, "'registry' should not be null");
    }

    public void record(final String action, final AuditOutcome outcome, final long durationNanos) {
        requireNonNull(action, "'action' should not be null");
        requireNonNull(outcome, "'outcome' should not be null");
        registry.counter("tickethub.usecase.executions", "action", action, "outcome", outcome.name())
                .increment();
        registry.timer("tickethub.usecase.duration", "action", action, "outcome", outcome.name())
                .record(Duration.ofNanos(Math.max(0, durationNanos)));
    }
}
