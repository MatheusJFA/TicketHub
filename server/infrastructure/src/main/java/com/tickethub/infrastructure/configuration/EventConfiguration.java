package com.tickethub.infrastructure.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import com.tickethub.domain.event.DomainEventPublisher;
import com.tickethub.infrastructure.events.KafkaDomainEventPublisher;

import tools.jackson.databind.ObjectMapper;

@Configuration(proxyBeanMethods = false)
public class EventConfiguration {

    @Bean
    @ConditionalOnBean(KafkaTemplate.class)
    DomainEventPublisher kafkaDomainEventPublisher(final KafkaTemplate<String, String> kafkaTemplate,
            final ObjectMapper objectMapper) {
        return new KafkaDomainEventPublisher(kafkaTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(DomainEventPublisher.class)
    DomainEventPublisher noopDomainEventPublisher() {
        return event -> {
        };
    }
}
