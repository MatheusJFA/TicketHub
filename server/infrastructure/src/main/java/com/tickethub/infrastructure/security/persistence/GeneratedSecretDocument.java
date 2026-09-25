package com.tickethub.infrastructure.security.persistence;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("app_secrets")
public record GeneratedSecretDocument(@Id String name, String value, Instant createdAt) {

    public static final String COLLECTION = "app_secrets";
}
