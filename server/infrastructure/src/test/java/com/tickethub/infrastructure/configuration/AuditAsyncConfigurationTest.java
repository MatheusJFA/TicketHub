package com.tickethub.infrastructure.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskExecutor;

class AuditAsyncConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AuditAsyncConfiguration.class);

    @Test
    void wiresAuditExecutor() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(TaskExecutor.class);
            assertThat(context).hasBean("auditExecutor");
        });
    }

    @Test
    void exposesAsyncUncaughtExceptionHandler() {
        runner.run(context -> {
            final var configuration = context.getBean(AuditAsyncConfiguration.class);
            assertThat(configuration.getAsyncUncaughtExceptionHandler()).isNotNull();
        });
    }
}
