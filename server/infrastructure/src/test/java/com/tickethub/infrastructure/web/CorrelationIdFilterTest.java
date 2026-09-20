package com.tickethub.infrastructure.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

@DisplayName("Correlation id filter")
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Given no headers, when filter, then generates correlation id and defaults actor")
    void givenNoHeaders_whenFilter_thenGeneratesCorrelationIdAndDefaultsActor() throws Exception {
        final var request = new MockHttpServletRequest();
        final var response = new MockHttpServletResponse();
        final var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertNotNull(response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
        assertNull(MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY));
    }

    @Test
    @DisplayName("Given headers, when filter, then propagates them")
    void givenHeaders_whenFilter_thenPropagatesThem() throws Exception {
        final var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, "corr-1");
        request.addHeader(CorrelationIdFilter.ACTOR_HEADER, "alice");
        final var response = new MockHttpServletResponse();
        final var seen = new String[2];
        final var chain = new MockFilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) {
                seen[0] = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
                seen[1] = MDC.get(CorrelationIdFilter.ACTOR_KEY);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals("corr-1", seen[0]);
        assertEquals("alice", seen[1]);
        assertEquals("corr-1", response.getHeader(CorrelationIdFilter.CORRELATION_ID_HEADER));
    }

    @Test
    @DisplayName("Given authenticated principal, when filter, then principal wins over actor header")
    void givenAuthenticatedPrincipal_whenFilter_thenPrincipalWinsOverActorHeader() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "jwt-user", null, List.of(new SimpleGrantedAuthority("show:create"))));
        final var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.ACTOR_HEADER, "spoofed");
        final var response = new MockHttpServletResponse();
        final var seen = new String[1];
        final var chain = new MockFilterChain() {
            @Override
            public void doFilter(final ServletRequest req, final ServletResponse res) {
                seen[0] = MDC.get(CorrelationIdFilter.ACTOR_KEY);
            }
        };

        filter.doFilter(request, response, chain);

        assertEquals("jwt-user", seen[0]);
    }
}
