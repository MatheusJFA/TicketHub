package com.tickethub.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void givenNoHeaders_whenFilter_thenGeneratesCorrelationIdAndDefaultsActor() throws Exception {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));
    }

    @Test
    void givenHeaders_whenFilter_thenPropagatesThem() throws Exception {
        final var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-1");
        request.addHeader(CorrelationIdFilter.ACTOR_HEADER, "alice");
        final var response = new MockHttpServletResponse();
        final var seen = new String[2];
        final var chain = new MockFilterChain() {
            @Override
            public void doFilter(final jakarta.servlet.ServletRequest req, final jakarta.servlet.ServletResponse res) {
                seen[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
                seen[1] = MDC.get(CorrelationIdFilter.ACTOR_KEY);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals("corr-1", seen[0]);
        assertEquals("alice", seen[1]);
        assertEquals("corr-1", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }
}
