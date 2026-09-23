package com.tickethub.infrastructure.configuration;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Offloads audit trail writes to a bounded pool so persistence latency never
 * blocks the request thread. Writes stay best-effort: saturation rejects with
 * a warning (already swallowed by the monitoring aspect) and background
 * failures are logged without propagating.
 */
@Configuration(proxyBeanMethods = false)
@EnableAsync(proxyTargetClass = true)
public class AuditAsyncConfiguration implements AsyncConfigurer {

    private static final Logger LOG = LoggerFactory.getLogger(AuditAsyncConfiguration.class);

    @Bean(name = "auditExecutor")
    TaskExecutor auditExecutor() {
        final var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("audit-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return AuditAsyncConfiguration::logFailure;
    }

    private static void logFailure(final Throwable failure, final Method method, final Object... parameters) {
        final var action = parameters.length == 0 ? Optional.<Object>empty() : Optional.ofNullable(parameters[0]);
        LOG.warn(
                "audit trail background write failed in {} for {}",
                method.getName(),
                action.map(Object::toString).orElse("unknown entry"),
                failure);
    }
}
