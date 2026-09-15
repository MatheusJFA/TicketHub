package com.tickethub.infrastructure.configuration;

import com.mongodb.client.MongoClient;
import com.tickethub.infrastructure.Main;
import java.util.ArrayList;
import java.util.UUID;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration",
        "tickethub.liquibase.enabled=true"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LiquibaseMigrationIT {
    private static final String DATABASE = "liquibase_it_" + UUID.randomUUID().toString().replace("-", "");
    private static final String CHANGELOG = "db/changelog/db.changelog-master.xml";
    @Autowired MongoClient client;
    @Autowired MongoDatabaseFactory factory;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.mongodb.database", () -> DATABASE);
    }

    @Test
    void migratesOnStartupAndDoesNotReapplyChangesOrCloseClient() throws Exception {
        final var database = factory.getMongoDatabase();
        final var collections = database.listCollectionNames().into(new ArrayList<>());
        assertTrue(collections.containsAll(java.util.List.of("customers", "partners", "shows", "sections", "spots")));
        final var history = database.getCollection("DATABASECHANGELOG");
        assertEquals(5, history.countDocuments());
        database.getCollection("customers").insertOne(new Document("_id", "preserved"));
        LiquibaseConfiguration.migrate(client, factory, CHANGELOG);
        assertEquals(5, history.countDocuments());
        assertNotNull(database.getCollection("customers").find(new Document("_id", "preserved")).first());
        assertEquals(1.0, database.runCommand(new Document("ping", 1)).getDouble("ok"));
    }

    @Test
    void propagatesMigrationFailures() {
        assertThrows(Exception.class, () -> LiquibaseConfiguration.migrate(client, factory, "missing-changelog.xml"));
    }

    @AfterAll
    void removeIsolatedDatabase() {
        assertEquals(DATABASE, factory.getMongoDatabase().getName());
        assertTrue(DATABASE.startsWith("liquibase_it_"));
        client.getDatabase(DATABASE).drop();
    }
}
