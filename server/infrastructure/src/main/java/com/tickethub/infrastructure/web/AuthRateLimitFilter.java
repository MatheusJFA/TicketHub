package com.tickethub.infrastructure.web;

import static java.util.Objects.requireNonNull;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.tickethub.infrastructure.authentication.AuthSessionProperties;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthRateLimitFilter extends AbstractRateLimitFilter {

    public AuthRateLimitFilter(final AuthSessionProperties properties) {
        super("auth", requireNonNull(properties, "'properties' should not be null")
                .getLoginRateLimitPerMinute());
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/auth/");
    }
}
