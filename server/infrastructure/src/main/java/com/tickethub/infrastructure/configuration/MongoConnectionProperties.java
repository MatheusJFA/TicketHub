package com.tickethub.infrastructure.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tickethub.mongo")
public record MongoConnectionProperties(
        Duration connectTimeout, Duration readTimeout, Duration serverSelectionTimeout) {}
