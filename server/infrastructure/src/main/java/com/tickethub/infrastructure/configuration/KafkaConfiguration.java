package com.tickethub.infrastructure.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.util.Assert;

@Configuration(proxyBeanMethods = false)
public class KafkaConfiguration {

    @Bean
    NewTopic eventsTopic(
            @Value("${tickethub.kafka.topic.name}") final String name,
            @Value("${tickethub.kafka.topic.partitions}") final int partitions,
            @Value("${tickethub.kafka.topic.replicas}") final int replicas) {
        Assert.hasText(name, "Kafka topic name must not be blank");
        Assert.isTrue(partitions > 0, "Kafka topic partitions must be positive");
        Assert.isTrue(
                replicas > 0 && replicas <= Short.MAX_VALUE,
                "Kafka topic replicas must be positive and within short range");
        return TopicBuilder.name(name).partitions(partitions).replicas(replicas).build();
    }
}
