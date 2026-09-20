package com.tickethub.infrastructure.configuration;

import static java.util.Objects.isNull;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.infrastructure.events.KafkaDomainEventPublisher;

import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class EventConfiguration {

    /**
     * Resolves the template lazily: {@code @ConditionalOnBean} cannot be used
     * here because user configurations are evaluated before the Kafka
     * auto-configuration, which would silently skip the publisher.
     */
    @Bean
    DomainEventPublisher domainEventPublisher(
            final ObjectProvider<KafkaTemplate<String, String>> templates,
            final ObjectProvider<ObjectMapper> mappers) {
        final var template = templates.getIfAvailable();
        if (isNull(template)) {
            return event -> {
            };
        }
        return new KafkaDomainEventPublisher(template, mappers.getIfAvailable(ObjectMapper::new));
    }
}
