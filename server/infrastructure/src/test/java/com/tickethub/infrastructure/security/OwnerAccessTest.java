package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class OwnerAccessTest {

    private final OwnerAccess access = new OwnerAccess();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticateAs(final String ownerId, final String... authorities) {
        final List<GrantedAuthority> granted = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new)
                .map(authority -> (GrantedAuthority) authority)
                .toList();
        final var jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("ownerId", ownerId));
        SecurityContextHolder.getContext()
                .setAuthentication(new JwtAuthenticationToken(jwt, granted));
    }

    @Test
    void givenOwner_whenIsSelfOrAdmin_thenReturnsTrue() {
        authenticateAs("customer-1", "customer:delete");

        assertTrue(access.isSelfOrAdmin("customer-1"));
    }

    @Test
    void givenAnotherAccount_whenIsSelfOrAdmin_thenReturnsFalse() {
        authenticateAs("customer-9", "customer:delete");

        assertFalse(access.isSelfOrAdmin("customer-1"));
    }

    @Test
    void givenAdminWithoutOwner_whenIsSelfOrAdmin_thenReturnsTrue() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        assertTrue(access.isSelfOrAdmin("customer-1"));
    }

    @Test
    void givenAnonymous_whenIsSelfOrAdmin_thenReturnsFalse() {
        assertFalse(access.isSelfOrAdmin("customer-1"));
    }

    @Test
    void givenNullId_whenIsSelfOrAdmin_thenReturnsFalse() {
        authenticateAs("customer-1", "customer:delete");

        assertFalse(access.isSelfOrAdmin(null));
    }
}
