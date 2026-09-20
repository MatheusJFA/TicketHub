package com.tickethub.infrastructure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.tickethub.infrastructure.ContainerSupport;
import com.tickethub.infrastructure.IntegrationTest;

@IntegrationTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Infrastructure connection")
class InfrastructureConnectionIT extends ContainerSupport {

    private static final String TEST_ID = UUID.randomUUID().toString().replace("-", "");
    private static final String TOPIC = "tickethub-integration-" + TEST_ID;
    private static final String COLLECTION = "integration_" + TEST_ID;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private ConsumerFactory<String, String> consumerFactory;
    @Autowired
    private KafkaAdmin kafkaAdmin;
    @Autowired
    private NewTopic eventsTopic;

    @DynamicPropertySource
    static void testProperties(final DynamicPropertyRegistry registry) {
        registry.add("tickethub.kafka.topic.name", () -> TOPIC);
        registry.add("tickethub.kafka.topic.partitions", () -> 1);
        registry.add("tickethub.kafka.topic.replicas", () -> 1);
        registry.add("spring.kafka.consumer.group-id", () -> "integration-" + TEST_ID);
    }

    @Test
    @DisplayName("Writes reads and deletes using auto configured Mongo template")
    void writesReadsAndDeletesUsingAutoConfiguredMongoTemplate() {
        final var document = new Document("_id", TEST_ID).append("message", "mongo-configured");
        mongoTemplate.insert(document, COLLECTION);
        final var stored = mongoTemplate.findById(TEST_ID, Document.class, COLLECTION);
        assertNotNull(stored);
        assertEquals("mongo-configured", stored.getString("message"));
        mongoTemplate.remove(document, COLLECTION);
        assertNull(mongoTemplate.findById(TEST_ID, Document.class, COLLECTION));
    }

    @Test
    @DisplayName("Creates topic and sends and receives using spring clients")
    void createsTopicAndSendsAndReceivesUsingSpringClients() throws Exception {
        final var topics = kafkaAdmin.describeTopics(TOPIC);
        assertEquals(eventsTopic.numPartitions(), topics.get(TOPIC).partitions().size());
        assertEquals(TOPIC, kafkaTemplate.getDefaultTopic());
        final var payload = "{\"event\":\"infrastructure-ready\"}";
        final var sendResult = kafkaTemplate.sendDefault(TEST_ID, payload).get(20, TimeUnit.SECONDS);
        final var metadata = sendResult.getRecordMetadata();
        final var partition = new TopicPartition(TOPIC, metadata.partition());
        try (final var consumer = consumerFactory.createConsumer()) {
            consumer.assign(List.of(partition));
            consumer.seek(partition, metadata.offset());
            final var deadline = System.nanoTime() + Duration.ofSeconds(20).toNanos();
            while (System.nanoTime() < deadline) {
                final var records = consumer.poll(Duration.ofSeconds(1));
                for (final var record : records) {
                    if (TEST_ID.equals(record.key())) {
                        assertEquals(payload, record.value());
                        return;
                    }
                }
            }
            fail("Kafka did not return the message sent through the configured template");
        }
    }

    @AfterAll
    void removeTestResources() throws Exception {
        mongoTemplate.dropCollection(COLLECTION);
        try (final var admin = Admin.create(kafkaAdmin.getConfigurationProperties())) {
            admin.deleteTopics(List.of(TOPIC)).all().get(20, TimeUnit.SECONDS);
        }
    }
}
