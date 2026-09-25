package com.tickethub.infrastructure.security;

import static java.util.Objects.requireNonNull;

import com.tickethub.domain.authentication.RevokedAccessTokenGateway;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

public class RevokedAccessTokenFilter extends OncePerRequestFilter {

    private final RevokedAccessTokenGateway revokedAccessTokens;

    public RevokedAccessTokenFilter(final RevokedAccessTokenGateway revokedAccessTokens) {
        this.revokedAccessTokens = requireNonNull(revokedAccessTokens, "'revokedAccessTokens' should not be null");
    }

    @Override
    protected void doFilterInternal(
            final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain)
            throws ServletException, IOException {
        final var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            final var tokenId = jwtAuthentication.getToken().getClaimAsString("jti");
            // Tokens without jti (tests, issued before rotation) are never denied.
            if (tokenId != null && revokedAccessTokens.isRevoked(tokenId)) {
                SecurityContextHolder.clearContext();
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token revoked");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
