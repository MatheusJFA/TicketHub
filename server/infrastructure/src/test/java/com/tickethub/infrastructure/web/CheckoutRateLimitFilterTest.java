package com.tickethub.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@DisplayName("Checkout rate limit filter")
class CheckoutRateLimitFilterTest {

    private MockHttpServletRequest request(final String method, final String uri) {
        final var request = new MockHttpServletRequest(method, uri);
        request.setRemoteAddr("127.0.0.1");
        return request;
    }

    private CheckoutRateLimitFilter filter(final int permits) {
        final var properties = new CheckoutProperties();
        properties.setOrderRateLimitPerMinute(permits);
        return new CheckoutRateLimitFilter(properties);
    }

    @Test
    @DisplayName("Given requests within limit, when filter, then forwards downstream")
    void givenRequestsWithinLimit_whenFilter_thenForwardsDownstream() throws Exception {
        final var filter = filter(2);
        final var forwarded = new AtomicInteger();
        final FilterChain chain = (req, res) -> forwarded.incrementAndGet();

        filter.doFilter(request("POST", "/orders"), new MockHttpServletResponse(), chain);

        assertEquals(1, forwarded.get());
    }

    @Test
    @DisplayName("Given order creation beyond limit, when filter, then returns 429")
    void givenOrdersBeyondLimit_whenFilter_thenReturns429() throws Exception {
        final var filter = filter(1);
        final FilterChain chain = (req, res) -> {};

        filter.doFilter(request("POST", "/orders"), new MockHttpServletResponse(), chain);
        final var response = new MockHttpServletResponse();
        filter.doFilter(request("POST", "/orders/order-1/pay"), response, chain);

        assertEquals(429, response.getStatus());
    }

    @Test
    @DisplayName("Given reads and other writes, when filter, then skips rate limit")
    void givenNonCheckoutPaths_whenFilter_thenSkipsRateLimit() throws Exception {
        final var filter = filter(1);
        final var forwarded = new AtomicInteger();
        final FilterChain chain = (req, res) -> forwarded.incrementAndGet();

        filter.doFilter(request("GET", "/shows"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("GET", "/shows"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("POST", "/orders/order-1/cancel"), new MockHttpServletResponse(), chain);
        filter.doFilter(request("POST", "/orders/order-1/cancel"), new MockHttpServletResponse(), chain);

        assertEquals(4, forwarded.get());
    }
}
