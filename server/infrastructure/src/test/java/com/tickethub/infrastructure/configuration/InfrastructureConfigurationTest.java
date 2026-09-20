package com.tickethub.infrastructure.configuration;

import java.util.concurrent.TimeUnit;

import com.mongodb.MongoClientSettings;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.boot.mongodb.autoconfigure.MongoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Infrastructure configuration")
class InfrastructureConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withUserConfiguration(MongoConfiguration.class, KafkaConfiguration.class, PropertiesConfiguration.class);

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({KafkaProperties.class, MongoProperties.class})
    static class PropertiesConfiguration {
    }

    @Test
    @DisplayName("Loads Mongo database and bounded timeouts")
    void loadsMongoDatabaseAndBoundedTimeouts() {
        contextRunner.withPropertyValues(
                "spring.mongodb.database=config-test",
                "tickethub.mongo.connect-timeout=2s",
                "tickethub.mongo.read-timeout=3s",
                "tickethub.mongo.server-selection-timeout=4s").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(MongoProperties.class).getDatabase()).isEqualTo("config-test");
            final var builder = MongoClientSettings.builder();
            context.getBean(MongoClientSettingsBuilderCustomizer.class).customize(builder);
            final var settings = builder.build();
            assertThat(settings.getSocketSettings().getConnectTimeout(TimeUnit.MILLISECONDS)).isEqualTo(2000);
            assertThat(settings.getSocketSettings().getReadTimeout(TimeUnit.MILLISECONDS)).isEqualTo(3000);
            assertThat(settings.getClusterSettings().getServerSelectionTimeout(TimeUnit.MILLISECONDS)).isEqualTo(4000);
        });
    }

    @Test
    @DisplayName("Loads kafka producer consumer and listener settings")
    void loadsKafkaProducerConsumerAndListenerSettings() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            final var kafka = context.getBean(KafkaProperties.class);
            assertThat(kafka.getProducer().getAcks()).isEqualTo("all");
            assertThat(kafka.getProducer().getProperties()).containsEntry("enable.idempotence", "true");
            assertThat(kafka.getProducer().getKeySerializer().getSimpleName()).isEqualTo("StringSerializer");
            assertThat(kafka.getProducer().getValueSerializer().getSimpleName()).isEqualTo("StringSerializer");
            assertThat(kafka.getConsumer().getEnableAutoCommit()).isFalse();
            assertThat(kafka.getConsumer().getAutoOffsetReset()).isEqualTo("earliest");
            assertThat(kafka.getConsumer().getValueDeserializer().getSimpleName()).isEqualTo("StringDeserializer");
            assertThat(kafka.getListener().getAckMode().name()).isEqualTo("RECORD");
            assertThat(kafka.getAdmin().isFailFast()).isTrue();
        });
    }

    @Test
    @DisplayName("Uses same configured topic for admin and template")
    void usesSameConfiguredTopicForAdminAndTemplate() {
        contextRunner.withPropertyValues(
                "tickethub.kafka.topic.name=custom.events",
                "tickethub.kafka.topic.partitions=2",
                "tickethub.kafka.topic.replicas=1",
                "spring.kafka.consumer.group-id=custom-group").run(context -> {
            assertThat(context).hasNotFailed();
            final var topic = context.getBean(NewTopic.class);
            assertThat(topic.name()).isEqualTo("custom.events");
            assertThat(topic.numPartitions()).isEqualTo(2);
            assertThat(topic.replicationFactor()).isEqualTo((short) 1);
            final var kafka = context.getBean(KafkaProperties.class);
            assertThat(kafka.getTemplate().getDefaultTopic()).isEqualTo(topic.name());
            assertThat(kafka.getConsumer().getGroupId()).isEqualTo("custom-group");
        });
    }

    @Test
    @DisplayName("Rejects unbounded Mongo timeout")
    void rejectsUnboundedMongoTimeout() {
        contextRunner.withPropertyValues("tickethub.mongo.read-timeout=0s")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    @DisplayName("Rejects invalid topic partition count")
    void rejectsInvalidTopicPartitionCount() {
        contextRunner.withPropertyValues("tickethub.kafka.topic.partitions=0")
                .run(context -> assertThat(context).hasFailed());
    }
}
