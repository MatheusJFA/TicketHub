package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.tickethub.domain.core.partner.PartnerID;
import com.tickethub.domain.core.show.Show;
import com.tickethub.domain.core.show.ShowGateway;
import com.tickethub.domain.core.show.ShowID;

class ShowAccessTest {

    private final ShowGateway gateway = mock(ShowGateway.class);
    private final ShowAccess access = new ShowAccess(gateway);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static void authenticateAsPartner(final String ownerId) {
        final var jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "none"), Map.of("ownerId", ownerId));
        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("show:write"))));
    }

    private static void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    private Show showOwnedBy(final String partnerId) {
        return Show.create("Concert", "Description", null, null, 10, PartnerID.from(partnerId));
    }

    @Test
    void givenOwner_whenCanWrite_thenReturnsTrue() {
        authenticateAsPartner("partner-1");
        final var showId = ShowID.generate();
        when(gateway.findById(showId)).thenReturn(Optional.of(showOwnedBy("partner-1")));

        assertTrue(access.canWrite(showId.getValue()));
    }

    @Test
    void givenAnotherPartner_whenCanWrite_thenReturnsFalse() {
        authenticateAsPartner("partner-9");
        final var showId = ShowID.generate();
        when(gateway.findById(showId)).thenReturn(Optional.of(showOwnedBy("partner-1")));

        assertFalse(access.canWrite(showId.getValue()));
    }

    @Test
    void givenAdmin_whenCanDelete_thenReturnsTrueWithoutLookup() {
        authenticateAsAdmin();

        assertTrue(access.canDelete("any-id"));
    }

    @Test
    void givenMissingShow_whenCanPublish_thenReturnsFalse() {
        authenticateAsPartner("partner-1");
        when(gateway.findById(any())).thenReturn(Optional.empty());

        assertFalse(access.canPublish("missing-id"));
    }

    @Test
    void givenGatewayFailure_whenCanWrite_thenReturnsFalse() {
        authenticateAsPartner("partner-1");
        when(gateway.findById(any())).thenThrow(new IllegalStateException("db down"));

        assertFalse(access.canWrite("show-1"));
    }

    @Test
    void givenOwner_whenCanCreate_thenReturnsTrue() {
        authenticateAsPartner("partner-1");

        assertTrue(access.canCreate("partner-1"));
        assertFalse(access.canCreate("partner-9"));
    }

    @Test
    void givenAnonymous_whenCanWrite_thenReturnsFalse() {
        assertFalse(access.canWrite("show-1"));
    }
}
