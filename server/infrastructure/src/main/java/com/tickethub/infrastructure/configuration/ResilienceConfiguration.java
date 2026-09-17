package com.tickethub.infrastructure.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tickethub.infrastructure.api.ApiSupport;
import com.tickethub.infrastructure.api.ResiliencePolicy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Builds the retry + circuit breaker policy from configuration and plugs it
 * into {@link ApiSupport}, the single choke point for use case execution.
 */
@Configuration(proxyBeanMethods = false)
public class ResilienceConfiguration {

    public ResilienceConfiguration(final ResiliencePolicy tickethubResiliencePolicy) {
        ApiSupport.configureResilience(tickethubResiliencePolicy);
    }

    @Bean
    public ResiliencePolicy tickethubResiliencePolicy(
            @Value("${tickethub.resilience.retry.max-attempts:3}") final int maxAttempts,
            @Value("${tickethub.resilience.retry.wait-ms:200}") final long retryWaitMs,
            @Value("${tickethub.resilience.circuit-breaker.failure-rate-threshold:50}") final float failureRateThreshold,
            @Value("${tickethub.resilience.circuit-breaker.wait-duration-ms:10000}") final long openStateWaitMs,
            @Value("${tickethub.resilience.circuit-breaker.sliding-window-size:20}") final int slidingWindowSize,
            @Value("${tickethub.resilience.circuit-breaker.minimum-calls:10}") final int minimumCalls) {
        final Retry retry = Retry.of("tickethub", RetryConfig.custom()
                .maxAttempts(maxAttempts)
                .waitDuration(Duration.ofMillis(retryWaitMs))
                .retryOnResult(ResiliencePolicy::isTransientResult)
                .ignoreExceptions(CallNotPermittedException.class)
                .build());
        final CircuitBreaker circuitBreaker = CircuitBreaker.of("tickethub", CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .waitDurationInOpenState(Duration.ofMillis(openStateWaitMs))
                .slidingWindowSize(slidingWindowSize)
                .minimumNumberOfCalls(minimumCalls)
                .recordResult(ResiliencePolicy::isTransientResult)
                .ignoreExceptions(CallNotPermittedException.class)
                .build());
        return ResiliencePolicy.of(retry, circuitBreaker);
    }
}
