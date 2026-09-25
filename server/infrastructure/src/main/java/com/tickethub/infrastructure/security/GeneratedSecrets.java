package com.tickethub.infrastructure.security;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.tickethub.infrastructure.security.persistence.GeneratedSecretDocument;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

public class GeneratedSecrets {

    static final String JWT_KEY = "jwt-secret";
    static final String TICKET_KEY = "ticket-signature-secret";

    private static final Logger log = LoggerFactory.getLogger(GeneratedSecrets.class);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String jwtConfigured;
    private final String ticketConfigured;
    private final MongoTemplate mongo;

    public GeneratedSecrets(final String jwtConfigured, final String ticketConfigured, final MongoTemplate mongo) {
        this.jwtConfigured = jwtConfigured;
        this.ticketConfigured = ticketConfigured;
        this.mongo = mongo;
    }

    public String jwtSecret() {
        return resolve(JWT_KEY, jwtConfigured, "TICKETHUB_JWT_SECRET");
    }

    public String ticketSignatureSecret() {
        return resolve(TICKET_KEY, ticketConfigured, "TICKETHUB_TICKETS_SIGNATURE_SECRET");
    }

    private String resolve(final String key, final String configured, final String envHint) {
        if (isNotBlank(configured)) {
            return configured;
        }
        if (mongo == null) {
            return random();
        }
        final var existing = mongo.findById(key, GeneratedSecretDocument.class, GeneratedSecretDocument.COLLECTION);
        if (existing != null) {
            return existing.value();
        }
        final var generated = random();
        try {
            mongo.save(new GeneratedSecretDocument(key, generated, Instant.now()), GeneratedSecretDocument.COLLECTION);
        } catch (final DuplicateKeyException e) {
            final var concurrent =
                    mongo.findById(key, GeneratedSecretDocument.class, GeneratedSecretDocument.COLLECTION);
            if (concurrent != null) {
                return concurrent.value();
            }
            throw e;
        }
        log.warn("Generated persistent secret '{}' on first boot — set the {} env var in production", key, envHint);
        return generated;
    }

    private static String random() {
        final var bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
