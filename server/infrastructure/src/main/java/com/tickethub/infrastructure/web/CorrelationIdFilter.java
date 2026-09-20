package com.tickethub.infrastructure.web;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.util.Optional;

import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String ACTOR_HEADER = "X-Actor";

    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String ACTOR_KEY = "actor";

    public static final String ANONYMOUS_ACTOR = "system";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain filterChain) throws ServletException, IOException {
        final String correlationId = headerOrGenerated(request, CORRELATION_ID_HEADER);
        final String actor = authenticatedPrincipal().orElseGet(() -> headerOrDefault(request, ACTOR_HEADER, ANONYMOUS_ACTOR));
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(ACTOR_KEY, actor);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_KEY);
            MDC.remove(ACTOR_KEY);
        }
    }

    private static Optional<String> authenticatedPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (isNull(authentication) || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(authentication.getName());
    }

    private static String headerOrGenerated(final HttpServletRequest request, final String header) {
        return Optional.ofNullable(request.getHeader(header)).filter(value -> !isBlank(value))
                .orElseGet(() -> UUID.randomUUID().toString());
    }

    private static String headerOrDefault(final HttpServletRequest request, final String header,
            final String fallback) {
        return Optional.ofNullable(request.getHeader(header)).filter(value -> !isBlank(value))
                .orElse(fallback);
    }
}
