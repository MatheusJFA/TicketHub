package com.tickethub.infrastructure.web;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Per-client (IP or X-Forwarded-For) fixed window backed by Redis, so the
 * budget holds across replicas. Rejected requests answer 429 with the
 * standard error envelope and never reach the controllers. Subclasses only
 * declare their path and budget.
 */
public abstract class AbstractRateLimitFilter extends OncePerRequestFilter {

    private final String name;
    private final int permitsPerMinute;
    private final RateLimitBudget budget;

    protected AbstractRateLimitFilter(final String name, final int permitsPerMinute, final RateLimitBudget budget) {
        this.name = requireNonNull(name, "'name' should not be null");
        this.permitsPerMinute = permitsPerMinute;
        this.budget = requireNonNull(budget, "'budget' should not be null");
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws ServletException, IOException {
        if (!budget.tryAcquire(name + ":" + clientKey(request), permitsPerMinute)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"errors\":[{\"message\":\"Too many requests\"}]}");
            return;
        }
        chain.doFilter(request, response);
    }

    private static String clientKey(final HttpServletRequest request) {
        final String forwarded = request.getHeader("X-Forwarded-For");
        if (isNotBlank(forwarded)) {
            // Filtra: primeiro IP da lista X-Forwarded-For separada por virgula.
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
