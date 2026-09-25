package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
@DisplayName("Revoked access token filter")
class RevokedAccessTokenFilterTest {

    @Mock
    private RevokedAccessTokenGateway revokedAccessTokens;

    @Mock
    private FilterChain chain;

    private RevokedAccessTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RevokedAccessTokenFilter(revokedAccessTokens);
    }

    @AfterEach
    void cleanUp() {
        SecurityContextHolder.clearContext();
    }

    private static JwtAuthenticationToken authentication(final String tokenId) {
        final var claims = new java.util.HashMap<String, Object>();
        claims.put("authorities", java.util.List.of("ROLE_CUSTOMER"));
        if (tokenId != null) {
            claims.put("jti", tokenId);
        }
        final var jwt = new Jwt(
                "token-value",
                java.time.Instant.now(),
                java.time.Instant.now().plusSeconds(3600),
                java.util.Map.of("alg", "HS256"),
                claims);
        return new JwtAuthenticationToken(
                jwt,
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    @Test
    @DisplayName("Given revoked token id, when filter, then returns unauthorized without chain")
    void givenRevokedTokenId_whenFilter_thenReturnsUnauthorizedWithoutChain() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication("jti-revoked"));
        when(revokedAccessTokens.isRevoked("jti-revoked")).thenReturn(true);

        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        assertEquals(401, response.getStatus());
        verify(chain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Given clean token id, when filter, then continues chain")
    void givenCleanTokenId_whenFilter_thenContinuesChain() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication("jti-clean"));
        when(revokedAccessTokens.isRevoked("jti-clean")).thenReturn(false);

        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Given token without jti, when filter, then continues without gateway")
    void givenTokenWithoutJti_whenFilter_thenContinuesWithoutGateway() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(authentication(null));

        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verify(revokedAccessTokens, never()).isRevoked(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Given anonymous, when filter, then continues chain")
    void givenAnonymous_whenFilter_thenContinuesChain() throws Exception {
        final var response = new MockHttpServletResponse();
        filter.doFilter(new MockHttpServletRequest(), response, chain);

        verify(chain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
