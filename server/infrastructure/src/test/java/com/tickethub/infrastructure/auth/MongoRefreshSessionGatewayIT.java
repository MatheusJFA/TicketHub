package com.tickethub.infrastructure.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.tickethub.domain.auth.RefreshSession;
import com.tickethub.domain.auth.SecureTokens;
import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;
import com.tickethub.infrastructure.MongoCleanUpExtension;
import com.tickethub.infrastructure.auth.persistence.RefreshSessionDocument;

@IntegrationTest
class MongoRefreshSessionGatewayIT extends ContainerSupport {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoRefreshSessionGateway gateway;

    @BeforeEach
    void cleanUp() {
        MongoCleanUpExtension.cleanCollections(mongoTemplate, RefreshSessionDocument.COLLECTION);
    }

    @Test
    void givenSession_whenSave_thenPersistsAndFindsByTokenHash() {
        final var session = RefreshSession.issue(SecureTokens.sha256Hex("token-1"), "maria@domain.com",
                List.of("ROLE_CUSTOMER"), "customer-1", Duration.ofDays(7));

        gateway.save(session);

        final var found = gateway.findByTokenHash(session.getTokenHash());
        assertTrue(found.isPresent());
        assertEquals(session.getId(), found.get().getId());
        assertEquals(session.getFamilyId(), found.get().getFamilyId());
        assertEquals("maria@domain.com", found.get().getSubject());
        assertEquals("customer-1", found.get().getOwnerId());
        assertTrue(found.get().isActive());
    }

    @Test
    void givenRotatedSession_whenSave_thenTracksRotationAndFamily() {
        final var current = RefreshSession.issue(SecureTokens.sha256Hex("token-1"), "maria@domain.com",
                List.of(), null, Duration.ofDays(7));
        gateway.save(current);
        final var next = current.rotate(SecureTokens.sha256Hex("token-2"), Duration.ofDays(7));
        gateway.save(current);
        gateway.save(next);

        final var family = gateway.findByFamilyId(current.getFamilyId());
        assertEquals(2, family.size());
        assertTrue(gateway.findByTokenHash(current.getTokenHash()).orElseThrow().isRotated());
        assertTrue(gateway.findByTokenHash(next.getTokenHash()).orElseThrow().isActive());
    }

    @Test
    void givenUnknownTokenHash_whenFindByTokenHash_thenReturnsEmpty() {
        assertTrue(gateway.findByTokenHash(SecureTokens.sha256Hex("unknown")).isEmpty());
    }
}
