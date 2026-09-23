package com.tickethub.infrastructure.audit;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("Use case metrics")
class UseCaseMetricsTest {

    @Test
    @DisplayName("Given executions, when record, then counts and times by action and outcome")
    void givenExecutions_whenRecord_thenCountsAndTimes() {
        final var registry = new SimpleMeterRegistry();
        final var metrics = new UseCaseMetrics(registry);

        metrics.record("DefaultCreateOrderUseCase", AuditOutcome.SUCCESS, 1_000_000);
        metrics.record("DefaultCreateOrderUseCase", AuditOutcome.SUCCESS, 2_000_000);
        metrics.record("DefaultConfirmPaymentUseCase", AuditOutcome.SUCCESS, 1_000_000);

        assertThat(registry.counter("tickethub.usecase.executions",
                "action", "DefaultCreateOrderUseCase", "outcome", "SUCCESS").count()).isEqualTo(2);
        assertThat(registry.counter("tickethub.usecase.executions",
                "action", "DefaultConfirmPaymentUseCase", "outcome", "SUCCESS").count()).isEqualTo(1);
        assertThat(registry.timer("tickethub.usecase.duration",
                "action", "DefaultCreateOrderUseCase", "outcome", "SUCCESS").count()).isEqualTo(2);
        assertThat(registry.timer("tickethub.usecase.duration",
                "action", "DefaultCreateOrderUseCase", "outcome", "SUCCESS")
                .totalTime(java.util.concurrent.TimeUnit.NANOSECONDS)).isEqualTo(3_000_000);
    }
}
