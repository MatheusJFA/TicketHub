package com.tickethub.infrastructure.web;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Per-client (IP or X-Forwarded-For) token bucket backed by Resilience4j.
 * Rejected requests answer 429 with the standard error envelope and never
 * reach the controllers. Subclasses only declare their path and budget.
 */
public abstract class AbstractRateLimitFilter extends OncePerRequestFilter {

    private final String name;
    private final RateLimiterConfig config;
    private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    protected AbstractRateLimitFilter(final String name, final int permitsPerMinute) {
        this.name = requireNonNull(name, "'name' should not be null");
        this.config = RateLimiterConfig.custom()
                .limitForPeriod(Math.max(1, permitsPerMinute))
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        final RateLimiter limiter = limiters.computeIfAbsent(clientKey(request),
                key -> RateLimiter.of(name + "-" + key, config));
        try {
            RateLimiter.decorateCheckedSupplier(limiter, () -> null).get();
        } catch (final RequestNotPermitted e) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"errors\":[{\"message\":\"Too many requests\"}]}");
            return;
        } catch (final Throwable e) {
            throw new ServletException(e);
        }
        chain.doFilter(request, response);
    }

    private static String clientKey(final HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (isNotBlank(forwarded)) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
