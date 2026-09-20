package com.tickethub.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.tickethub.infrastructure.authentication.AuthSessionProperties;

import jakarta.servlet.FilterChain;

@DisplayName("Auth rate limit filter")
class AuthRateLimitFilterTest {

    private MockHttpServletRequest request(final String uri) {
        final var request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    @Test
    @DisplayName("Given requests within limit, when filter, then forwards downstream")
    void givenRequestsWithinLimit_whenFilter_thenForwardsDownstream() throws Exception {
        final var properties = new AuthSessionProperties();
        properties.setLoginRateLimitPerMinute(2);
        final var filter = new AuthRateLimitFilter(properties);
        final var forwarded = new AtomicInteger();
        final FilterChain chain = (req, res) -> forwarded.incrementAndGet();

        filter.doFilter(request("/auth/login"), new MockHttpServletResponse(), chain);

        assertEquals(1, forwarded.get());
    }

    @Test
    @DisplayName("Given requests beyond limit, when filter, then returns429")
    void givenRequestsBeyondLimit_whenFilter_thenReturns429() throws Exception {
        final var properties = new AuthSessionProperties();
        properties.setLoginRateLimitPerMinute(1);
        final var filter = new AuthRateLimitFilter(properties);
        final FilterChain chain = (req, res) -> {
        };

        filter.doFilter(request("/auth/login"), new MockHttpServletResponse(), chain);
        final var response = new MockHttpServletResponse();
        filter.doFilter(request("/auth/login"), response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    @DisplayName("Given non auth path, when filter, then skips rate limit")
    void givenNonAuthPath_whenFilter_thenSkipsRateLimit() throws Exception {
        final var properties = new AuthSessionProperties();
        properties.setLoginRateLimitPerMinute(1);
        final var filter = new AuthRateLimitFilter(properties);
        final var forwarded = new AtomicInteger();
        final FilterChain chain = (req, res) -> forwarded.incrementAndGet();

        filter.doFilter(request("/customers"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("/customers"), new MockHttpServletResponse(), chain);

        assertEquals(2, forwarded.get());
    }
}
