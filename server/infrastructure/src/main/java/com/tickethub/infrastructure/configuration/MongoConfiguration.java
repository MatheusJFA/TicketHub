package com.tickethub.infrastructure.configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MongoConnectionProperties.class)
public class MongoConfiguration {

    @Bean
    MongoClientSettingsBuilderCustomizer mongoTimeouts(final MongoConnectionProperties properties) {
        final var connectMillis = positiveMillis(properties.connectTimeout());
        final var readMillis = positiveMillis(properties.readTimeout());
        final var selectionMillis = positiveMillis(properties.serverSelectionTimeout());
        return builder -> builder.applyToSocketSettings(
                        socket -> socket.connectTimeout(connectMillis, TimeUnit.MILLISECONDS)
                                .readTimeout(readMillis, TimeUnit.MILLISECONDS))
                .applyToClusterSettings(
                        cluster -> cluster.serverSelectionTimeout(selectionMillis, TimeUnit.MILLISECONDS));
    }

    private static int positiveMillis(final Duration duration) {
        final var millis = duration.toMillis();
        Assert.isTrue(
                millis > 0 && millis <= Integer.MAX_VALUE,
                "MongoDB timeout must be a positive number of milliseconds within integer range");
        return (int) millis;
    }
}
