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
import com.tickethub.infrastructure.section.persistence.SectionDocument;
import com.tickethub.infrastructure.spot.persistence.SpotDocument;

class ShowAccessTest {

    private final ShowGateway gateway = mock(ShowGateway.class);
    private final org.springframework.data.mongodb.core.MongoTemplate mongoTemplate =
            mock(org.springframework.data.mongodb.core.MongoTemplate.class);
    private final ShowAccess access = new ShowAccess(gateway, mongoTemplate);

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

    private static SectionDocument sectionOwnedBy(final String sectionId, final String partnerId) {
        return new SectionDocument(sectionId, "VIP", "Front", false, 2, 0, null, List.of(),
                "show-1", partnerId, Instant.now(), Instant.now(), null, null, null);
    }

    private static SpotDocument spotOwnedBy(final String spotId, final String partnerId) {
        return new SpotDocument(spotId, "A1", true, false, "show-1", "section-1", partnerId,
                Instant.now(), Instant.now(), null, null, null);
    }

    @Test
    void givenOwner_whenCanWriteSection_thenReturnsTrueWithSingleRead() {
        authenticateAsPartner("partner-1");
        when(mongoTemplate.findById("section-1", SectionDocument.class, SectionDocument.COLLECTION))
                .thenReturn(sectionOwnedBy("section-1", "partner-1"));

        assertTrue(access.canWriteSection("section-1"));
    }

    @Test
    void givenAnotherPartner_whenCanWriteSection_thenReturnsFalse() {
        authenticateAsPartner("partner-9");
        when(mongoTemplate.findById("section-1", SectionDocument.class, SectionDocument.COLLECTION))
                .thenReturn(sectionOwnedBy("section-1", "partner-1"));

        assertFalse(access.canWriteSection("section-1"));
        assertFalse(access.canPublishSection("section-1"));
        assertFalse(access.canDeleteSection("section-1"));
    }

    @Test
    void givenOrphanSection_whenCanWriteSection_thenReturnsFalse() {
        authenticateAsPartner("partner-1");
        when(mongoTemplate.findById("section-1", SectionDocument.class, SectionDocument.COLLECTION))
                .thenReturn(new SectionDocument("section-1", "VIP", "Front", false, 0, 0, null, List.of(),
                        null, null, Instant.now(), Instant.now(), null, null, null));
        when(mongoTemplate.findOne(any(), any(), any())).thenReturn(null);

        assertFalse(access.canWriteSection("section-1"));
    }

    @Test
    void givenOwner_whenCanWriteSpot_thenReturnsTrueWithSingleRead() {
        authenticateAsPartner("partner-1");
        when(mongoTemplate.findById("spot-1", SpotDocument.class, SpotDocument.COLLECTION))
                .thenReturn(spotOwnedBy("spot-1", "partner-1"));

        assertTrue(access.canWriteSpot("spot-1"));
    }

    @Test
    void givenAnotherPartner_whenCanPublishSpot_thenReturnsFalse() {
        authenticateAsPartner("partner-9");
        when(mongoTemplate.findById("spot-1", SpotDocument.class, SpotDocument.COLLECTION))
                .thenReturn(spotOwnedBy("spot-1", "partner-1"));

        assertFalse(access.canPublishSpot("spot-1"));
        assertFalse(access.canDeleteSpot("spot-1"));
    }

    @Test
    void givenAdmin_whenCanWriteSectionOrSpot_thenReturnsTrueWithoutLookup() {
        authenticateAsAdmin();

        assertTrue(access.canWriteSection("any-section"));
        assertTrue(access.canWriteSpot("any-spot"));
    }
}
