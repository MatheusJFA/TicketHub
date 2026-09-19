package com.tickethub.infrastructure.web;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tickethub.infrastructure.auth.AuthSessionProperties;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterConfig config;
    private final ConcurrentMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(final AuthSessionProperties properties) {
        Objects.requireNonNull(properties, "'properties' should not be null");
        final int permits = Math.max(1, properties.getLoginRateLimitPerMinute());
        this.config = RateLimiterConfig.custom()
                .limitForPeriod(permits)
                .limitRefreshPeriod(Duration.ofMinutes(1))
                .timeoutDuration(Duration.ZERO)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws ServletException, IOException {
        final RateLimiter limiter = limiters.computeIfAbsent(clientKey(request),
                key -> RateLimiter.of("auth-" + key, config));
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
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
