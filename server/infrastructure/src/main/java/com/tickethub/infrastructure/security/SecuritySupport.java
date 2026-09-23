package com.tickethub.infrastructure.security;

import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

final class SecuritySupport {

    static final String ADMIN_ROLE = "ROLE_ADMIN";
    static final String OWNER_ID_CLAIM = "ownerId";

    private SecuritySupport() {}

    static Optional<Authentication> authentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated);
    }

    static boolean isAdmin() {
        return authentication().map(Authentication::getAuthorities).orElseGet(List::of).stream()
                .anyMatch(authority -> ADMIN_ROLE.equals(authority.getAuthority()));
    }

    static Optional<String> ownerId() {
        return authentication()
                .filter(JwtAuthenticationToken.class::isInstance)
                .map(JwtAuthenticationToken.class::cast)
                .map(token -> token.getToken().getClaimAsString(OWNER_ID_CLAIM));
    }
}
