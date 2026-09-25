package com.tickethub.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tickethub.infrastructure.security.persistence.GeneratedSecretDocument;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("Generated secrets")
class GeneratedSecretsTest {

    @Mock
    private MongoTemplate mongo;

    @Test
    @DisplayName("Given configured secret, when resolve, then uses it without mongo")
    void givenConfiguredSecret_whenResolve_thenUsesItWithoutMongo() {
        final var secrets = new GeneratedSecrets("explicit-jwt-secret", "explicit-ticket-secret", mongo);

        assertEquals("explicit-jwt-secret", secrets.jwtSecret());
        assertEquals("explicit-ticket-secret", secrets.ticketSignatureSecret());
        verify(mongo, never()).findById(any(), any(), any());
    }

    @Test
    @DisplayName("Given blank secret, when resolve, then generates and persists")
    void givenBlankSecret_whenResolve_thenGeneratesAndPersists() {
        when(mongo.findById("jwt-secret", GeneratedSecretDocument.class, "app_secrets"))
                .thenReturn(null);
        final var secrets = new GeneratedSecrets(null, null, mongo);

        final var first = secrets.jwtSecret();

        assertNotNull(first);
        assertTrue(first.getBytes(StandardCharsets.UTF_8).length >= 32);
        verify(mongo).save(any(GeneratedSecretDocument.class), eq("app_secrets"));
    }

    @Test
    @DisplayName("Given persisted secret, when resolve, then reuses it")
    void givenPersistedSecret_whenResolve_thenReusesIt() {
        when(mongo.findById("ticket-signature-secret", GeneratedSecretDocument.class, "app_secrets"))
                .thenReturn(new GeneratedSecretDocument("ticket-signature-secret", "persisted", Instant.now()));
        final var secrets = new GeneratedSecrets(null, null, mongo);

        assertEquals("persisted", secrets.ticketSignatureSecret());
        verify(mongo, never()).save(any(), any());
    }

    @Test
    @DisplayName("Given blank secret without mongo, when resolve, then generates ephemeral")
    void givenBlankSecretWithoutMongo_whenResolve_thenGeneratesEphemeral() {
        final var secrets = new GeneratedSecrets(null, null, null);

        assertNotEquals(secrets.jwtSecret(), secrets.jwtSecret());
    }
}
